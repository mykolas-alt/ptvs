import { useState } from 'react'
import { useAuth } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import '../styles/Dashboard.css'

type Status = 'active' | 'ended' | 'pending'

type Service = {
  id: number
  name: string
  vendor: string
  startDate: string
  endDate: string
  status: Status
  price: number
  contact: { name: string; email: string; phone: string }
  responsible: { name: string; department: string }
}

const MOCK_SERVICES: Service[] = [
  {
    id: 1,
    name: 'AWS Cloud',
    vendor: 'Amazon Web Services',
    startDate: '2023-01-15',
    endDate: '2025-01-14',
    status: 'active',
    price: 1200,
    contact: { name: 'John Smith', email: 'support@aws.amazon.com', phone: '+1-800-555-0100' },
    responsible: { name: 'Alice Johnson', department: 'IT Infrastructure' },
  },
  {
    id: 2,
    name: 'GitHub Enterprise',
    vendor: 'GitHub Inc.',
    startDate: '2022-06-01',
    endDate: '2024-05-31',
    status: 'ended',
    price: 250,
    contact: { name: 'Support Team', email: 'enterprise@github.com', phone: '+1-800-555-0200' },
    responsible: { name: 'Bob Williams', department: 'Development' },
  },
  {
    id: 3,
    name: 'Slack Business+',
    vendor: 'Salesforce / Slack',
    startDate: '2026-03-01',
    endDate: '2027-02-28',
    status: 'pending',
    price: 180,
    contact: { name: 'Slack Sales', email: 'sales@slack.com', phone: '+1-800-555-0300' },
    responsible: { name: 'Carol Martinez', department: 'Operations' },
  },
  {
    id: 4,
    name: 'Jira Cloud',
    vendor: 'Atlassian',
    startDate: '2024-04-01',
    endDate: '2026-03-31',
    status: 'active',
    price: 320,
    contact: { name: 'Atlassian Support', email: 'support@atlassian.com', phone: '+1-800-555-0400' },
    responsible: { name: 'David Lee', department: 'Project Management' },
  },
  {
    id: 5,
    name: 'Zoom Pro',
    vendor: 'Zoom Video Communications',
    startDate: '2021-09-01',
    endDate: '2023-08-31',
    status: 'ended',
    price: 150,
    contact: { name: 'Zoom Support', email: 'support@zoom.us', phone: '+1-800-555-0500' },
    responsible: { name: 'Eve Chen', department: 'Communications' },
  },
]

const STATUS_CLASS: Record<Status, string> = {
  active: 'status-active',
  ended: 'status-ended',
  pending: 'status-pending',
}

export function Dashboard() {
  const { userInfo, isLoading, logout } = useAuth()
  const [statusFilter, setStatusFilter] = useState<Status | 'all'>('all')
  const [selected, setSelected] = useState<Service | null>(null)

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  const filtered =
    statusFilter === 'all' ? MOCK_SERVICES : MOCK_SERVICES.filter(s => s.status === statusFilter)

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
              onChange={e => setStatusFilter(e.target.value as Status | 'all')}
              className="filter-select"
            >
              <option value="all">All</option>
              <option value="active">Active</option>
              <option value="pending">Pending</option>
              <option value="ended">Ended</option>
            </select>
          </div>
        </div>

        <div className="table-wrapper">
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
                    <td>{service.name}</td>
                    <td>{service.vendor}</td>
                    <td>{service.startDate}</td>
                    <td>{service.endDate}</td>
                    <td>
                      <span className={`status-badge ${STATUS_CLASS[service.status]}`}>
                        {service.status.charAt(0).toUpperCase() + service.status.slice(1)}
                      </span>
                    </td>
                    <td>${service.price.toLocaleString()}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </main>

      {selected && (
        <div className="modal-overlay" onClick={() => setSelected(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{selected.name}</h2>
              <button className="modal-close" onClick={() => setSelected(null)}>✕</button>
            </div>
            <div className="modal-body">
              <section className="modal-section">
                <h3>Contact Information</h3>
                <p><strong>Name:</strong> {selected.contact.name}</p>
                <p><strong>Email:</strong> {selected.contact.email}</p>
                <p><strong>Phone:</strong> {selected.contact.phone}</p>
              </section>
              <section className="modal-section">
                <h3>Responsible Personnel</h3>
                <p><strong>Name:</strong> {selected.responsible.name}</p>
                <p><strong>Department:</strong> {selected.responsible.department}</p>
              </section>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
