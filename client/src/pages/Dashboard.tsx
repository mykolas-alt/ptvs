import { useState, useEffect } from 'react'
import { useAuth, tokenStorageKey } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import '../styles/Dashboard.css'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'

type ServiceStatus = 'ACTIVE' | 'ENDED' | 'PENDING'

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
  status: ServiceStatus
  vendorContact: VendorContact | null
  responsiblePersonnel: Employee[]
}

const STATUS_CLASS: Record<ServiceStatus, string> = {
  ACTIVE: 'status-active',
  ENDED: 'status-ended',
  PENDING: 'status-pending',
}

export function Dashboard() {
  const { userInfo, isLoading, logout } = useAuth()
  const [services, setServices] = useState<Service[]>([])
  const [servicesLoading, setServicesLoading] = useState(true)
  const [servicesError, setServicesError] = useState<string | null>(null)
  const [statusFilter, setStatusFilter] = useState<ServiceStatus | 'all'>('all')
  const [selected, setSelected] = useState<Service | null>(null)

  useEffect(() => {
    if (isLoading) return
    const token = localStorage.getItem(tokenStorageKey)
    fetch(`${apiBaseUrl}/services`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then(r => {
        if (!r.ok) throw new Error()
        return r.json() as Promise<Service[]>
      })
      .then(data => {
        setServices(data)
        setServicesLoading(false)
      })
      .catch(() => {
        setServicesError('Could not load services.')
        setServicesLoading(false)
      })
  }, [isLoading])

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  const filtered =
    statusFilter === 'all' ? services : services.filter(s => s.status === statusFilter)

  return (
    <div className="page-container">
      <Navbar userInfo={userInfo} onLogout={logout} />

      <main className="dashboard-main">
        <div className="dashboard-toolbar">
          <h1>Third-Party Services</h1>
          <div className="filter-group">
            <label htmlFor="status-filter">Filter by status</label>
            <select
              id="status-filter"
              value={statusFilter}
              onChange={e => setStatusFilter(e.target.value as ServiceStatus | 'all')}
              className="filter-select"
            >
              <option value="all">All</option>
              <option value="ACTIVE">Active</option>
              <option value="PENDING">Pending</option>
              <option value="ENDED">Ended</option>
            </select>
          </div>
        </div>

        <div className="table-wrapper">
          {servicesLoading ? (
            <p className="loading-text">Loading services...</p>
          ) : servicesError ? (
            <p className="loading-text">{servicesError}</p>
          ) : (
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
                {filtered.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="empty-row">No services match the selected filter.</td>
                  </tr>
                ) : (
                  filtered.map(service => (
                    <tr key={service.id} className="service-row" onClick={() => setSelected(service)}>
                      <td>{service.serviceName}</td>
                      <td>{service.vendorContact?.vendorName ?? '—'}</td>
                      <td>{service.contractStartDate}</td>
                      <td>{service.contractEndDate}</td>
                      <td>
                        <span className={`status-badge ${STATUS_CLASS[service.status]}`}>
                          {service.status.charAt(0) + service.status.slice(1).toLowerCase()}
                        </span>
                      </td>
                      <td>${Number(service.monthlyCost).toLocaleString()}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          )}
        </div>
      </main>

      {selected && (
        <div className="modal-overlay" onClick={() => setSelected(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{selected.serviceName}</h2>
              <button className="modal-close" onClick={() => setSelected(null)}>✕</button>
            </div>
            <div className="modal-body">
              <section className="modal-section">
                <h3>Contact Information</h3>
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
          </div>
        </div>
      )}
    </div>
  )
}
