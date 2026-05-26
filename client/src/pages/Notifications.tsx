import { useState, useEffect, useCallback } from 'react'
import { useAuth, tokenStorageKey } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import '../styles/Notifications.css'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'

type Employee = {
  id: string
  name: string
  department: string
}

type Service = {
  id: string
  serviceName: string
  vendorContact: { vendorName: string } | null
  status: string
  responsiblePersonnel: Employee[]
}

type EmployeeConfig = {
  id: string
  employeeId: string
  serviceId: string
  daysBeforeExpiry: number
  additionalEmails: string[]
}

type AddForm = {
  employeeId: string
  daysBeforeExpiry: string
  additionalEmails: string
}

type EditForm = {
  daysBeforeExpiry: string
  additionalEmails: string
}

export function Notifications() {
  const { userInfo, isLoading, logout } = useAuth()
  const isAdmin = userInfo?.roles.includes('ADMIN') ?? false

  const [services, setServices] = useState<Service[]>([])
  const [servicesLoading, setServicesLoading] = useState(true)
  const [selectedService, setSelectedService] = useState<Service | null>(null)

  const [configs, setConfigs] = useState<EmployeeConfig[]>([])
  const [configsLoading, setConfigsLoading] = useState(false)

  const [addForm, setAddForm] = useState<AddForm>({ employeeId: '', daysBeforeExpiry: '30', additionalEmails: '' })
  const [addError, setAddError] = useState<string | null>(null)
  const [addSaving, setAddSaving] = useState(false)

  const [editingId, setEditingId] = useState<string | null>(null)
  const [editForm, setEditForm] = useState<EditForm>({ daysBeforeExpiry: '', additionalEmails: '' })
  const [editError, setEditError] = useState<string | null>(null)
  const [editSaving, setEditSaving] = useState(false)

  function getToken() { return localStorage.getItem(tokenStorageKey) }

  useEffect(() => {
    if (isLoading) return
    fetch(`${apiBaseUrl}/services?size=10000&statuses=ACTIVE`, {
      headers: { Authorization: `Bearer ${getToken()}` },
    })
      .then(r => r.ok ? r.json() as Promise<{ content: Service[] }> : Promise.resolve({ content: [] as Service[] }))
      .then(page => { setServices(page.content ?? []); setServicesLoading(false) })
      .catch(() => setServicesLoading(false))
  }, [isLoading])

  const loadConfigs = useCallback(async (serviceId: string) => {
    setConfigsLoading(true)
    try {
      const res = await fetch(`${apiBaseUrl}/notifications/services/${serviceId}/employee-config`, {
        headers: { Authorization: `Bearer ${getToken()}` },
      })
      setConfigs(res.ok ? await res.json() as EmployeeConfig[] : [])
    } catch {
      setConfigs([])
    } finally {
      setConfigsLoading(false)
    }
  }, [])

  function selectService(svc: Service) {
    setSelectedService(svc)
    setEditingId(null)
    setAddForm({ employeeId: '', daysBeforeExpiry: '30', additionalEmails: '' })
    setAddError(null)
    loadConfigs(svc.id)
  }

  function employeeName(employeeId: string) {
    const emp = selectedService?.responsiblePersonnel.find(e => e.id === employeeId)
    return emp ? `${emp.name} (${emp.department})` : employeeId
  }

  function parseEmails(raw: string): string[] {
    return raw.split(',').map(s => s.trim()).filter(Boolean)
  }

  async function handleAdd() {
    if (!selectedService) return
    if (!addForm.employeeId) { setAddError('Select an employee.'); return }
    const days = Number(addForm.daysBeforeExpiry)
    if (!days || days <= 0) { setAddError('Days before expiry must be greater than 0.'); return }
    setAddSaving(true)
    setAddError(null)
    try {
      const res = await fetch(`${apiBaseUrl}/notifications/services/${selectedService.id}/employee-config`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${getToken()}` },
        body: JSON.stringify({
          employeeId: addForm.employeeId,
          daysBeforeExpiry: days,
          additionalEmails: parseEmails(addForm.additionalEmails),
        }),
      })
      if (!res.ok) {
        const err = await res.json().catch(() => null) as { message?: string } | null
        throw new Error(err?.message ?? 'Failed to add config.')
      }
      const created = await res.json() as EmployeeConfig
      setConfigs(prev => [...prev, created])
      setAddForm({ employeeId: '', daysBeforeExpiry: '30', additionalEmails: '' })
    } catch (e) {
      setAddError(e instanceof Error ? e.message : 'Failed to add.')
    } finally {
      setAddSaving(false)
    }
  }

  function startEdit(config: EmployeeConfig) {
    setEditingId(config.id)
    setEditForm({
      daysBeforeExpiry: String(config.daysBeforeExpiry),
      additionalEmails: config.additionalEmails.join(', '),
    })
    setEditError(null)
  }

  async function handleSaveEdit(config: EmployeeConfig) {
    if (!selectedService) return
    const days = Number(editForm.daysBeforeExpiry)
    if (!days || days <= 0) { setEditError('Days before expiry must be greater than 0.'); return }
    setEditSaving(true)
    setEditError(null)
    try {
      const res = await fetch(`${apiBaseUrl}/notifications/services/${selectedService.id}/employee-config/${config.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${getToken()}` },
        body: JSON.stringify({
          daysBeforeExpiry: days,
          additionalEmails: parseEmails(editForm.additionalEmails),
        }),
      })
      if (!res.ok) throw new Error('Failed to update.')
      const updated = await res.json() as EmployeeConfig
      setConfigs(prev => prev.map(c => c.id === config.id ? updated : c))
      setEditingId(null)
    } catch (e) {
      setEditError(e instanceof Error ? e.message : 'Update failed.')
    } finally {
      setEditSaving(false)
    }
  }

  async function handleDelete(config: EmployeeConfig) {
    if (!selectedService) return
    try {
      const res = await fetch(`${apiBaseUrl}/notifications/services/${selectedService.id}/employee-config/${config.id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${getToken()}` },
      })
      if (!res.ok) throw new Error()
      setConfigs(prev => prev.filter(c => c.id !== config.id))
    } catch {
      alert('Failed to delete config.')
    }
  }

  const configuredEmployeeIds = new Set(configs.map(c => c.employeeId))
  const availableToAdd = selectedService?.responsiblePersonnel.filter(e => !configuredEmployeeIds.has(e.id)) ?? []

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  return (
    <div className="page-container">
      <Navbar userInfo={userInfo} onLogout={logout} />
      <main className="notifications-main notifications-main-wide">
        <h1>Notification Settings</h1>

        {!isAdmin ? (
          <p className="notif-hint">You need admin privileges to manage notification settings.</p>
        ) : (
          <div className="notif-layout">
            <aside className="notif-service-list">
              <div className="notif-aside-header">Active Services</div>
              {servicesLoading ? (
                <p className="loading-text notif-aside-loading">Loading...</p>
              ) : services.length === 0 ? (
                <p className="notif-hint notif-aside-empty">No active services.</p>
              ) : (
                services.map(svc => (
                  <button
                    key={svc.id}
                    className={`notif-service-item${selectedService?.id === svc.id ? ' notif-service-item-active' : ''}`}
                    onClick={() => selectService(svc)}
                  >
                    <span className="notif-service-name">{svc.serviceName}</span>
                    {svc.vendorContact && (
                      <span className="notif-service-vendor">{svc.vendorContact.vendorName}</span>
                    )}
                  </button>
                ))
              )}
            </aside>

            <div className="notif-config-panel">
              {!selectedService ? (
                <p className="notif-hint notif-panel-empty">Select a service to manage its notification settings.</p>
              ) : (
                <>
                  <div className="notif-panel-header">
                    <h2>{selectedService.serviceName}</h2>
                    {selectedService.vendorContact && (
                      <span className="notif-panel-sub">{selectedService.vendorContact.vendorName}</span>
                    )}
                  </div>

                  {configsLoading ? (
                    <p className="loading-text">Loading...</p>
                  ) : (
                    <>
                      {configs.length === 0 && (
                        <p className="notif-hint notif-hint-block">No notification configs yet. Add one below.</p>
                      )}

                      {configs.map(config => (
                        <div key={config.id} className="notif-config-row">
                          <div className="notif-config-employee">{employeeName(config.employeeId)}</div>

                          {editingId === config.id ? (
                            <div className="notif-edit-inline">
                              <div className="notif-inline-fields">
                                <div className="notif-inline-field">
                                  <label>Days before expiry</label>
                                  <input
                                    type="number"
                                    min="1"
                                    className="notif-input-sm"
                                    value={editForm.daysBeforeExpiry}
                                    onChange={e => setEditForm(f => ({ ...f, daysBeforeExpiry: e.target.value }))}
                                  />
                                </div>
                                <div className="notif-inline-field notif-inline-emails">
                                  <label>Additional emails (comma-separated)</label>
                                  <input
                                    type="text"
                                    className="notif-input"
                                    value={editForm.additionalEmails}
                                    onChange={e => setEditForm(f => ({ ...f, additionalEmails: e.target.value }))}
                                    placeholder="e.g. manager@co.com"
                                  />
                                </div>
                              </div>
                              {editError && <p className="notif-error">{editError}</p>}
                              <div className="notif-config-actions">
                                <button className="btn-primary btn-sm" onClick={() => handleSaveEdit(config)} disabled={editSaving}>
                                  {editSaving ? 'Saving…' : 'Save'}
                                </button>
                                <button className="btn-secondary btn-sm" onClick={() => setEditingId(null)}>Cancel</button>
                              </div>
                            </div>
                          ) : (
                            <div className="notif-config-details">
                              <span className="notif-config-days">{config.daysBeforeExpiry}d before expiry</span>
                              {config.additionalEmails.length > 0 && (
                                <span className="notif-config-emails">{config.additionalEmails.join(', ')}</span>
                              )}
                              <div className="notif-config-actions">
                                <button className="btn-secondary btn-sm" onClick={() => startEdit(config)}>Edit</button>
                                <button className="btn-danger btn-sm" onClick={() => handleDelete(config)}>Remove</button>
                              </div>
                            </div>
                          )}
                        </div>
                      ))}

                      <div className="notif-add-section">
                        <div className="notif-add-header">Add Employee Config</div>
                        {availableToAdd.length === 0 ? (
                          <p className="notif-hint">All responsible employees already have a config.</p>
                        ) : (
                          <>
                            <div className="notif-add-row">
                              <select
                                value={addForm.employeeId}
                                onChange={e => setAddForm(f => ({ ...f, employeeId: e.target.value }))}
                                className="notif-select"
                              >
                                <option value="">— select employee —</option>
                                {availableToAdd.map(e => (
                                  <option key={e.id} value={e.id}>{e.name} ({e.department})</option>
                                ))}
                              </select>
                              <div className="notif-inline-field">
                                <label>Days before expiry</label>
                                <input
                                  type="number"
                                  min="1"
                                  className="notif-input-sm"
                                  value={addForm.daysBeforeExpiry}
                                  onChange={e => setAddForm(f => ({ ...f, daysBeforeExpiry: e.target.value }))}
                                />
                              </div>
                            </div>
                            <div className="notif-field">
                              <label>Additional emails (comma-separated)</label>
                              <input
                                type="text"
                                className="notif-input"
                                value={addForm.additionalEmails}
                                onChange={e => setAddForm(f => ({ ...f, additionalEmails: e.target.value }))}
                                placeholder="e.g. manager@co.com, it@co.com"
                              />
                            </div>
                            {addError && <p className="notif-error">{addError}</p>}
                            <button className="btn-primary btn-sm-add" onClick={handleAdd} disabled={addSaving}>
                              {addSaving ? 'Adding…' : 'Add'}
                            </button>
                          </>
                        )}
                      </div>
                    </>
                  )}
                </>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  )
}
