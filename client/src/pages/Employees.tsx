import { useState, useEffect } from 'react'
import type { FormEvent } from 'react'
import { useAuth, tokenStorageKey } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import '../styles/ManagePage.css'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'

type Employee = {
  id: string
  name: string
  email: string
  phone: string
  department: string
  jobTitle: string
}

type EmpForm = {
  name: string
  email: string
  phone: string
  department: string
  jobTitle: string
}

type EmpErrors = Partial<Record<keyof EmpForm, string>>

function validate(f: EmpForm): EmpErrors {
  const e: EmpErrors = {}
  if (!f.name.trim()) e.name = 'Name is required.'
  if (!f.email.trim()) e.email = 'Email is required.'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email)) e.email = 'Email is invalid.'
  if (!f.department.trim()) e.department = 'Department is required.'
  return e
}

const EMPTY: EmpForm = { name: '', email: '', phone: '', department: '', jobTitle: '' }

export function Employees() {
  const { userInfo, isLoading, logout } = useAuth()
  const [employees, setEmployees] = useState<Employee[]>([])
  const [listLoading, setListLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState<EmpForm>(EMPTY)
  const [errors, setErrors] = useState<EmpErrors>({})
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  function getToken() { return localStorage.getItem(tokenStorageKey) }

  useEffect(() => {
    if (isLoading) return
    fetch(`${apiBaseUrl}/employees?size=10000`, {
      headers: { Authorization: `Bearer ${getToken()}` },
    })
      .then(r => r.ok ? r.json() as Promise<{ content: Employee[] }> : Promise.resolve({ content: [] as Employee[] }))
      .then(page => { setEmployees(Array.isArray(page.content) ? page.content : []); setListLoading(false) })
      .catch(() => setListLoading(false))
  }, [isLoading])

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  function setField(field: keyof EmpForm, value: string) {
    setForm(prev => ({ ...prev, [field]: value }))
    if (errors[field]) setErrors(prev => ({ ...prev, [field]: undefined }))
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    const errs = validate(form)
    if (Object.keys(errs).length > 0) { setErrors(errs); return }

    setIsSubmitting(true)
    setSubmitError(null)
    try {
      const res = await fetch(`${apiBaseUrl}/employees`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${getToken()}`,
        },
        body: JSON.stringify(form),
      })
      if (!res.ok) throw new Error('Failed to create employee.')
      const created = await res.json() as Employee
      setEmployees(prev => [created, ...prev])
      setForm(EMPTY)
      setErrors({})
      setShowForm(false)
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Failed to create employee.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="page-container">
      <Navbar userInfo={userInfo} onLogout={logout} />
      <main className="manage-main">
        <div className="manage-toolbar">
          <h1>Employees</h1>
          <button className="btn-primary btn-sm-add" onClick={() => setShowForm(v => !v)}>
            {showForm ? 'Cancel' : '+ Add Employee'}
          </button>
        </div>

        {showForm && (
          <form className="manage-form" onSubmit={handleSubmit} noValidate>
            <div className="manage-form-grid">
              <div className={`form-field${errors.name ? ' has-error' : ''}`}>
                <label>Name</label>
                <input type="text" value={form.name} onChange={e => setField('name', e.target.value)} placeholder="e.g. Alice Johnson" />
                {errors.name && <span className="field-error">{errors.name}</span>}
              </div>
              <div className={`form-field${errors.email ? ' has-error' : ''}`}>
                <label>Email</label>
                <input type="email" value={form.email} onChange={e => setField('email', e.target.value)} placeholder="e.g. alice@company.com" />
                {errors.email && <span className="field-error">{errors.email}</span>}
              </div>
              <div className="form-field">
                <label>Phone</label>
                <input type="text" value={form.phone} onChange={e => setField('phone', e.target.value)} placeholder="e.g. +370 600 00000" />
              </div>
              <div className={`form-field${errors.department ? ' has-error' : ''}`}>
                <label>Department</label>
                <input type="text" value={form.department} onChange={e => setField('department', e.target.value)} placeholder="e.g. IT Infrastructure" />
                {errors.department && <span className="field-error">{errors.department}</span>}
              </div>
              <div className="form-field">
                <label>Job Title</label>
                <input type="text" value={form.jobTitle} onChange={e => setField('jobTitle', e.target.value)} placeholder="e.g. Systems Administrator" />
              </div>
            </div>
            {submitError && <p className="field-error">{submitError}</p>}
            <button type="submit" className="btn-primary btn-submit" disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save Employee'}
            </button>
          </form>
        )}

        <div className="table-wrapper">
          {listLoading ? (
            <p className="loading-text">Loading employees...</p>
          ) : (
            <table className="services-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Department</th>
                  <th>Job Title</th>
                  <th>Phone</th>
                </tr>
              </thead>
              <tbody>
                {employees.length === 0 ? (
                  <tr><td colSpan={5} className="empty-row">No employees yet.</td></tr>
                ) : employees.map(emp => (
                  <tr key={emp.id}>
                    <td>{emp.name}</td>
                    <td>{emp.email}</td>
                    <td>{emp.department}</td>
                    <td>{emp.jobTitle || '—'}</td>
                    <td>{emp.phone || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </main>
    </div>
  )
}
