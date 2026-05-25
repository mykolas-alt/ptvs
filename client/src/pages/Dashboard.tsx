import { useState, useEffect, useCallback } from 'react'
import { useAuth, tokenStorageKey } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import { OptimisticLockConflictModal } from '../components/OptimisticLockConflictModal'
import '../styles/Dashboard.css'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'

type ServiceStatus = 'ACTIVE' | 'PENDING' | 'DEACTIVATED' | 'EXPIRED'

type VendorContact = {
  id: string
  name: string
  email: string
  phone: string
  vendorName: string
}

type Employee = {
  id: string
  name: string
  department: string
}

type Service = {
  id: string
  serviceName: string
  monthlyCost: number
  contractStartDate: string
  contractEndDate: string
  manualDeactivatedAt: string | null
  status: ServiceStatus
  vendorContact: VendorContact | null
  responsiblePersonnel: Employee[]
  version: number
}

type EditForm = {
  serviceName: string
  monthlyCost: string
  contractStartDate: string
  contractEndDate: string
  vendorContactId: string
  responsiblePersonnelIds: string[]
}

const STATUS_CLASS: Record<ServiceStatus, string> = {
  ACTIVE: 'status-active',
  PENDING: 'status-pending',
  DEACTIVATED: 'status-deactivated',
  EXPIRED: 'status-expired',
}

const ALL_STATUSES: ServiceStatus[] = ['ACTIVE', 'PENDING', 'DEACTIVATED', 'EXPIRED']
const PAGE_SIZE = 10

export function Dashboard() {
  const { userInfo, isLoading, logout } = useAuth()
  const [services, setServices] = useState<Service[]>([])
  const [totalPages, setTotalPages] = useState(1)
  const [servicesLoading, setServicesLoading] = useState(true)
  const [servicesError, setServicesError] = useState<string | null>(null)
  const [statusFilters, setStatusFilters] = useState<ServiceStatus[]>([])
  const [selected, setSelected] = useState<Service | null>(null)
  const [page, setPage] = useState(1)

  const [allContacts, setAllContacts] = useState<VendorContact[]>([])
  const [allEmployees, setAllEmployees] = useState<Employee[]>([])
  const [isEditing, setIsEditing] = useState(false)
  const [editForm, setEditForm] = useState<EditForm | null>(null)
  const [editError, setEditError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [showConflict, setShowConflict] = useState(false)
  const [conflictLoading, setConflictLoading] = useState(false)

  function getToken() {
    return localStorage.getItem(tokenStorageKey)
  }

  const loadServices = useCallback(async (p: number, filters: ServiceStatus[]) => {
    setServicesLoading(true)
    setServicesError(null)
    try {
      const token = getToken()
      const params = new URLSearchParams({ page: String(p - 1), size: String(PAGE_SIZE) })
      filters.forEach(s => params.append('statuses', s))
      const res = await fetch(`${apiBaseUrl}/services?${params}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      if (!res.ok) throw new Error()
      const data = await res.json() as { content: Service[], totalPages: number }
      setServices(Array.isArray(data.content) ? data.content : [])
      setTotalPages(data.totalPages ?? 1)
    } catch {
      setServicesError('Could not load services.')
    } finally {
      setServicesLoading(false)
    }
  }, [])

  useEffect(() => {
    if (isLoading) return
    const token = getToken()
    const headers = { Authorization: `Bearer ${token}` }
    Promise.all([
      fetch(`${apiBaseUrl}/vendor-contacts?size=10000`, { headers }).then(r => r.ok ? r.json() as Promise<{ content: VendorContact[] }> : Promise.resolve({ content: [] as VendorContact[] })),
      fetch(`${apiBaseUrl}/employees?size=10000`, { headers }).then(r => r.ok ? r.json() as Promise<{ content: Employee[] }> : Promise.resolve({ content: [] as Employee[] })),
    ]).then(([contactsPage, employeesPage]) => {
      setAllContacts(Array.isArray(contactsPage.content) ? contactsPage.content : [])
      setAllEmployees(Array.isArray(employeesPage.content) ? employeesPage.content : [])
    }).catch(() => {})
  }, [isLoading])

  useEffect(() => {
    if (isLoading) return
    loadServices(page, statusFilters)
  }, [isLoading, page, statusFilters, loadServices])

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  function toggleStatusFilter(s: ServiceStatus) {
    setStatusFilters(prev =>
      prev.includes(s) ? prev.filter(x => x !== s) : [...prev, s]
    )
    setPage(1)
  }

  function openEdit(svc: Service) {
    setEditForm({
      serviceName: svc.serviceName,
      monthlyCost: String(svc.monthlyCost),
      contractStartDate: svc.contractStartDate,
      contractEndDate: svc.contractEndDate,
      vendorContactId: svc.vendorContact?.id ?? '',
      responsiblePersonnelIds: svc.responsiblePersonnel.map(e => e.id),
    })
    setEditError(null)
    setIsEditing(true)
  }

  async function handleDeactivate(svc: Service) {
    const token = getToken()
    try {
      const res = await fetch(`${apiBaseUrl}/services/${svc.id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      })
      if (!res.ok) throw new Error()
      setSelected({ ...svc, status: 'DEACTIVATED' })
      await loadServices(page, statusFilters)
    } catch {
      alert('Failed to deactivate service.')
    }
  }

  function buildEditPayload(form: EditForm, svc: Service, forceUpdate = false) {
    return {
      serviceName: form.serviceName,
      monthlyCost: Number(form.monthlyCost),
      contractStartDate: form.contractStartDate,
      contractEndDate: form.contractEndDate,
      vendorContactId: form.vendorContactId,
      responsiblePersonnelIds: form.responsiblePersonnelIds,
      version: svc.version,
      forceUpdate,
    }
  }

  async function submitEdit(forceUpdate = false) {
    if (!editForm || !selected) return
    const token = getToken()
    const res = await fetch(`${apiBaseUrl}/services/${selected.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: JSON.stringify(buildEditPayload(editForm, selected, forceUpdate)),
    })
    if (res.status === 409) {
      setShowConflict(true)
      return
    }
    if (!res.ok) throw new Error()
    const updated = await res.json() as Service
    setSelected(updated)
    setIsEditing(false)
    setShowConflict(false)
    await loadServices(page, statusFilters)
  }

  async function handleEditSubmit() {
    if (!editForm || !selected) return
    if (!editForm.serviceName.trim()) { setEditError('Service name is required.'); return }
    if (!editForm.monthlyCost || Number(editForm.monthlyCost) <= 0) { setEditError('Monthly cost must be greater than 0.'); return }
    if (!editForm.contractStartDate) { setEditError('Start date is required.'); return }
    if (!editForm.contractEndDate || editForm.contractEndDate <= editForm.contractStartDate) { setEditError('End date must be after start date.'); return }
    if (!editForm.vendorContactId) { setEditError('Vendor contact is required.'); return }
    if (editForm.responsiblePersonnelIds.length === 0) { setEditError('At least one responsible person is required.'); return }

    setIsSubmitting(true)
    setEditError(null)
    try {
      await submitEdit()
    } catch {
      setEditError('Failed to update service.')
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleConflictReload() {
    if (!selected) return
    setConflictLoading(true)
    const token = getToken()
    try {
      const res = await fetch(`${apiBaseUrl}/services/${selected.id}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      if (!res.ok) throw new Error()
      const fresh = await res.json() as Service
      setSelected(fresh)
      setEditForm({
        serviceName: fresh.serviceName,
        monthlyCost: String(fresh.monthlyCost),
        contractStartDate: fresh.contractStartDate,
        contractEndDate: fresh.contractEndDate,
        vendorContactId: fresh.vendorContact?.id ?? '',
        responsiblePersonnelIds: fresh.responsiblePersonnel.map(e => e.id),
      })
      setShowConflict(false)
    } catch {
      setEditError('Failed to reload service data.')
      setShowConflict(false)
    } finally {
      setConflictLoading(false)
    }
  }

  async function handleConflictForce() {
    setConflictLoading(true)
    try {
      await submitEdit(true)
    } catch {
      setEditError('Force update failed.')
      setShowConflict(false)
    } finally {
      setConflictLoading(false)
    }
  }

  function toggleEmployee(id: string) {
    if (!editForm) return
    const ids = editForm.responsiblePersonnelIds
    setEditForm({
      ...editForm,
      responsiblePersonnelIds: ids.includes(id) ? ids.filter(x => x !== id) : [...ids, id],
    })
  }

  const safePage = Math.min(page, Math.max(1, totalPages))

  return (
    <div className="page-container">
      <Navbar userInfo={userInfo} onLogout={logout} />

      <main className="dashboard-main">
        <div className="dashboard-toolbar">
          <h1>Third-Party Services</h1>
          <div className="filter-group">
            <span className="filter-label">Filter:</span>
            {ALL_STATUSES.map(s => (
              <button
                key={s}
                type="button"
                className={`filter-chip${statusFilters.includes(s) ? ' filter-chip-on' : ''}`}
                onClick={() => toggleStatusFilter(s)}
              >
                {s.charAt(0) + s.slice(1).toLowerCase()}
              </button>
            ))}
          </div>
        </div>

        <div className="table-wrapper">
          {servicesLoading ? (
            <p className="loading-text">Loading services...</p>
          ) : servicesError ? (
            <p className="loading-text">{servicesError}</p>
          ) : (
            <>
              <table className="services-table">
                <thead>
                  <tr>
                    <th>Service Name</th>
                    <th>Vendor</th>
                    <th>Contract Start</th>
                    <th>Contract Expiry</th>
                    <th>Status</th>
                    <th>Price / Month</th>
                  </tr>
                </thead>
                <tbody>
                  {services.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="empty-row">No services match the selected filter.</td>
                    </tr>
                  ) : (
                    services.map(service => (
                      <tr key={service.id} className="service-row" onClick={() => { setSelected(service); setIsEditing(false) }}>
                        <td>{service.serviceName}</td>
                        <td>{service.vendorContact?.vendorName ?? '—'}</td>
                        <td>{service.contractStartDate}</td>
                        <td>
                          {service.contractEndDate}
                          {service.manualDeactivatedAt && (
                            <span className="deactivated-date"> (deactivated {service.manualDeactivatedAt})</span>
                          )}
                        </td>
                        <td>
                          <span className={`status-badge ${STATUS_CLASS[service.status]}`}>
                            {service.status.charAt(0) + service.status.slice(1).toLowerCase()}
                          </span>
                        </td>
                        <td>€{Number(service.monthlyCost).toLocaleString()}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>

              {totalPages > 1 && (
                <div className="pagination">
                  <button
                    className="page-btn"
                    onClick={() => setPage(1)}
                    disabled={safePage === 1}
                  >«</button>
                  <button
                    className="page-btn"
                    onClick={() => setPage(p => Math.max(1, p - 1))}
                    disabled={safePage === 1}
                  >‹</button>
                  <span className="page-info">{safePage} / {totalPages}</span>
                  <button
                    className="page-btn"
                    onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                    disabled={safePage === totalPages}
                  >›</button>
                  <button
                    className="page-btn"
                    onClick={() => setPage(totalPages)}
                    disabled={safePage === totalPages}
                  >»</button>
                </div>
              )}
            </>
          )}
        </div>
      </main>

      {showConflict && (
        <OptimisticLockConflictModal
          entityLabel="service"
          onReload={handleConflictReload}
          onForce={handleConflictForce}
          onClose={() => setShowConflict(false)}
          isLoading={conflictLoading}
        />
      )}

      {selected && (
        <div className="modal-overlay" onClick={() => { setSelected(null); setIsEditing(false) }}>
          <div className="modal modal-wide" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{selected.serviceName}</h2>
              <div className="modal-header-actions">
                {!isEditing && (
                  <>
                    <button className="btn-secondary btn-sm" onClick={() => openEdit(selected)}>Edit</button>
                    {selected.status !== 'DEACTIVATED' && selected.status !== 'EXPIRED' && (
                      <button
                        className="btn-sm btn-deactivate"
                        onClick={() => handleDeactivate(selected)}
                      >
                        Deactivate
                      </button>
                    )}
                  </>
                )}
                <button className="modal-close" onClick={() => { setSelected(null); setIsEditing(false) }}>✕</button>
              </div>
            </div>

            {isEditing && editForm ? (
              <div className="modal-body">
                <section className="modal-section">
                  <h3>Edit Service</h3>
                  <div className="edit-field">
                    <label>Service Name</label>
                    <input value={editForm.serviceName} onChange={e => setEditForm({ ...editForm, serviceName: e.target.value })} />
                  </div>
                  <div className="edit-field">
                    <label>Monthly Cost (€)</label>
                    <input type="number" min="0.01" step="0.01" value={editForm.monthlyCost} onChange={e => setEditForm({ ...editForm, monthlyCost: e.target.value })} />
                  </div>
                  <div className="edit-field-row">
                    <div className="edit-field">
                      <label>Contract Start</label>
                      <input type="date" value={editForm.contractStartDate} onChange={e => setEditForm({ ...editForm, contractStartDate: e.target.value })} />
                    </div>
                    <div className="edit-field">
                      <label>Contract End</label>
                      <input type="date" value={editForm.contractEndDate} onChange={e => setEditForm({ ...editForm, contractEndDate: e.target.value })} />
                    </div>
                  </div>
                  <div className="edit-field">
                    <label>Vendor Contact</label>
                    <select value={editForm.vendorContactId} onChange={e => setEditForm({ ...editForm, vendorContactId: e.target.value })}>
                      <option value="">— select contact —</option>
                      {allContacts.map(c => (
                        <option key={c.id} value={c.id}>{c.name} ({c.vendorName})</option>
                      ))}
                    </select>
                  </div>
                  <div className="edit-field">
                    <label>Responsible Personnel</label>
                    <div className="employee-checklist">
                      {allEmployees.map(e => (
                        <label key={e.id} className="emp-check">
                          <input
                            type="checkbox"
                            checked={editForm.responsiblePersonnelIds.includes(e.id)}
                            onChange={() => toggleEmployee(e.id)}
                          />
                          {e.name} — {e.department}
                        </label>
                      ))}
                    </div>
                  </div>
                  {editError && <p className="edit-error">{editError}</p>}
                  <div className="edit-actions">
                    <button className="btn-primary btn-sm" onClick={handleEditSubmit} disabled={isSubmitting}>
                      {isSubmitting ? 'Saving…' : 'Save'}
                    </button>
                    <button className="btn-secondary btn-sm" onClick={() => setIsEditing(false)}>Cancel</button>
                  </div>
                </section>
              </div>
            ) : (
              <div className="modal-body">
                <section className="modal-section">
                  <h3>Service Details</h3>
                  <p><strong>Status:</strong>{' '}
                    <span className={`status-badge ${STATUS_CLASS[selected.status]}`}>
                      {selected.status.charAt(0) + selected.status.slice(1).toLowerCase()}
                    </span>
                  </p>
                  <p><strong>Monthly Cost:</strong> €{Number(selected.monthlyCost).toLocaleString()}</p>
                  <p><strong>Start:</strong> {selected.contractStartDate}</p>
                  <p><strong>Expiry:</strong> {selected.contractEndDate}</p>
                  {selected.manualDeactivatedAt && (
                    <p><strong>Deactivated:</strong> {selected.manualDeactivatedAt}</p>
                  )}
                </section>
                <section className="modal-section">
                  <h3>Contact Information</h3>
                  <p><strong>Vendor:</strong> {selected.vendorContact?.vendorName ?? '—'}</p>
                  <p><strong>Name:</strong> {selected.vendorContact?.name ?? '—'}</p>
                  <p><strong>Email:</strong> {selected.vendorContact?.email ?? '—'}</p>
                  <p><strong>Phone:</strong> {selected.vendorContact?.phone ?? '—'}</p>
                </section>
                <section className="modal-section">
                  <h3>Responsible Personnel</h3>
                  {selected.responsiblePersonnel?.length > 0 ? (
                    selected.responsiblePersonnel.map(emp => (
                      <div key={emp.id}>
                        <p><strong>Name:</strong> {emp.name}</p>
                        <p><strong>Department:</strong> {emp.department}</p>
                      </div>
                    ))
                  ) : (
                    <p>—</p>
                  )}
                </section>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
