import { useState, useEffect } from 'react'
import type { FormEvent } from 'react'
import { useAuth, tokenStorageKey } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import '../styles/ServiceRegistration.css'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'

type VendorContact = { id: string; name: string; email: string; phone: string; vendorName: string }
type Employee = { id: string; name: string; email: string; department: string }

type ServiceForm = {
  name: string
  price: string
  startDate: string
  endDate: string
}

type NewContact = { vendor: string; contactName: string; contactEmail: string; contactPhone: string }
type NewEmployee = { empName: string; empEmail: string; empDepartment: string }

type ServiceErrors = Partial<Record<keyof ServiceForm, string>>

function validateService(f: ServiceForm): ServiceErrors {
  const e: ServiceErrors = {}
  if (!f.name.trim()) e.name = 'Service name is required.'
  if (!f.price) e.price = 'Monthly cost is required.'
  else if (Number(f.price) <= 0) e.price = 'Monthly cost must be greater than 0.'
  if (!f.startDate) e.startDate = 'Start date is required.'
  if (!f.endDate) e.endDate = 'End date is required.'
  else if (f.startDate && f.endDate <= f.startDate) e.endDate = 'End date must be after start date.'
  return e
}

export function ServiceRegistration() {
  const { userInfo, isLoading, logout } = useAuth()

  const [contacts, setContacts] = useState<VendorContact[]>([])
  const [employees, setEmployees] = useState<Employee[]>([])
  const [dataLoading, setDataLoading] = useState(true)

  const [form, setForm] = useState<ServiceForm>({ name: '', price: '', startDate: '', endDate: '' })
  const [errors, setErrors] = useState<ServiceErrors>({})

  // Contact selection
  const [contactMode, setContactMode] = useState<'existing' | 'new'>('existing')
  const [selectedContactId, setSelectedContactId] = useState('')
  const [newContact, setNewContact] = useState<NewContact>({ vendor: '', contactName: '', contactEmail: '', contactPhone: '' })
  const [contactError, setContactError] = useState<string | null>(null)

  // Employee selection
  const [employeeMode, setEmployeeMode] = useState<'existing' | 'new'>('existing')
  const [selectedEmployeeIds, setSelectedEmployeeIds] = useState<string[]>([])
  const [newEmployee, setNewEmployee] = useState<NewEmployee>({ empName: '', empEmail: '', empDepartment: '' })
  const [employeeError, setEmployeeError] = useState<string | null>(null)

  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [submitted, setSubmitted] = useState(false)
  const [submittedName, setSubmittedName] = useState('')

  function getToken() { return localStorage.getItem(tokenStorageKey) }

  useEffect(() => {
    if (isLoading) return
    const token = getToken()
    const headers = { Authorization: `Bearer ${token}` }
    Promise.all([
      fetch(`${apiBaseUrl}/vendor-contacts?size=10000`, { headers }).then(r => r.ok ? r.json() as Promise<{ content: VendorContact[] }> : Promise.resolve({ content: [] as VendorContact[] })),
      fetch(`${apiBaseUrl}/employees?size=10000`, { headers }).then(r => r.ok ? r.json() as Promise<{ content: Employee[] }> : Promise.resolve({ content: [] as Employee[] })),
    ]).then(([c, e]) => {
      setContacts(Array.isArray(c.content) ? c.content : [])
      setEmployees(Array.isArray(e.content) ? e.content : [])
      setDataLoading(false)
    }).catch(() => setDataLoading(false))
  }, [isLoading])

  const isAdmin = userInfo?.roles.includes('ADMIN') ?? false

  if (isLoading || dataLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  function setField(field: keyof ServiceForm, value: string) {
    setForm(prev => ({ ...prev, [field]: value }))
    if (errors[field]) setErrors(prev => ({ ...prev, [field]: undefined }))
  }

  function toggleEmployee(id: string) {
    setSelectedEmployeeIds(prev =>
      prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]
    )
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    const errs = validateService(form)
    if (Object.keys(errs).length > 0) { setErrors(errs); return }

    let contactOk = true
    let employeeOk = true

    if (contactMode === 'existing' && !selectedContactId) {
      setContactError('Please select a vendor contact or create a new one.')
      contactOk = false
    } else {
      setContactError(null)
    }

    if (contactMode === 'new') {
      if (!newContact.vendor.trim() || !newContact.contactName.trim() || !newContact.contactEmail.trim() || !newContact.contactPhone.trim()) {
        setContactError('All contact fields are required.')
        contactOk = false
      } else {
        setContactError(null)
      }
    }

    if (employeeMode === 'existing' && selectedEmployeeIds.length === 0) {
      setEmployeeError('Please select at least one employee or create a new one.')
      employeeOk = false
    } else {
      setEmployeeError(null)
    }

    if (employeeMode === 'new') {
      if (!newEmployee.empName.trim() || !newEmployee.empEmail.trim() || !newEmployee.empDepartment.trim()) {
        setEmployeeError('Name, email and department are required.')
        employeeOk = false
      } else {
        setEmployeeError(null)
      }
    }

    if (!contactOk || !employeeOk) return

    setIsSubmitting(true)
    setSubmitError(null)

    const token = getToken()
    const headers = { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` }

    try {
      let vcId = selectedContactId
      if (contactMode === 'new') {
        const vcRes = await fetch(`${apiBaseUrl}/vendor-contacts`, {
          method: 'POST',
          headers,
          body: JSON.stringify({
            name: newContact.contactName,
            email: newContact.contactEmail,
            phone: newContact.contactPhone,
            vendorName: newContact.vendor,
          }),
        })
        if (!vcRes.ok) throw new Error('Failed to create vendor contact.')
        const vc = await vcRes.json() as { id: string }
        vcId = vc.id
      }

      let empIds = selectedEmployeeIds
      if (employeeMode === 'new') {
        const empRes = await fetch(`${apiBaseUrl}/employees`, {
          method: 'POST',
          headers,
          body: JSON.stringify({
            name: newEmployee.empName,
            email: newEmployee.empEmail,
            department: newEmployee.empDepartment,
          }),
        })
        if (!empRes.ok) throw new Error('Failed to create employee record.')
        const emp = await empRes.json() as { id: string }
        empIds = [emp.id]
      }

      const svcRes = await fetch(`${apiBaseUrl}/services`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          serviceName: form.name,
          monthlyCost: Number(form.price),
          contractStartDate: form.startDate,
          contractEndDate: form.endDate,
          vendorContactId: vcId,
          responsiblePersonnelIds: empIds,
        }),
      })
      if (!svcRes.ok) throw new Error('Failed to register service.')

      setSubmittedName(form.name)
      setSubmitted(true)
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Registration failed.')
    } finally {
      setIsSubmitting(false)
    }
  }

  function reset() {
    setForm({ name: '', price: '', startDate: '', endDate: '' })
    setErrors({})
    setContactMode('existing')
    setSelectedContactId('')
    setNewContact({ vendor: '', contactName: '', contactEmail: '', contactPhone: '' })
    setContactError(null)
    setEmployeeMode('existing')
    setSelectedEmployeeIds([])
    setNewEmployee({ empName: '', empEmail: '', empDepartment: '' })
    setEmployeeError(null)
    setSubmitError(null)
    setSubmitted(false)
    setSubmittedName('')
  }

  return (
    <div className="page-container">
      <Navbar userInfo={userInfo} onLogout={logout} />
      <main className="form-main">
        <h1>Register Third-Party Service</h1>

        {!isAdmin ? (
          <div className="success-card">
            <p>You do not have permission to register services.</p>
          </div>
        ) : submitted ? (
          <div className="success-card">
            <h2>Service Registered</h2>
            <p><strong>{submittedName}</strong> has been successfully registered.</p>
            <button className="btn-primary" onClick={reset}>Register Another</button>
          </div>
        ) : (
          <form className="service-form" onSubmit={handleSubmit} noValidate>
            <fieldset>
              <legend>Service Details</legend>
              <div className={`form-field${errors.name ? ' has-error' : ''}`}>
                <label>Service Name</label>
                <input type="text" value={form.name} onChange={e => setField('name', e.target.value)} placeholder="e.g. AWS Cloud" />
                {errors.name && <span className="field-error">{errors.name}</span>}
              </div>
              <div className={`form-field${errors.price ? ' has-error' : ''}`}>
                <label>Monthly Cost (€)</label>
                <input type="number" min="0.01" step="0.01" value={form.price} onChange={e => setField('price', e.target.value)} placeholder="0.00" />
                {errors.price && <span className="field-error">{errors.price}</span>}
              </div>
              <div className="field-row">
                <div className={`form-field${errors.startDate ? ' has-error' : ''}`}>
                  <label>Contract Start Date</label>
                  <input type="date" value={form.startDate} onChange={e => setField('startDate', e.target.value)} />
                  {errors.startDate && <span className="field-error">{errors.startDate}</span>}
                </div>
                <div className={`form-field${errors.endDate ? ' has-error' : ''}`}>
                  <label>Contract End Date</label>
                  <input type="date" value={form.endDate} onChange={e => setField('endDate', e.target.value)} />
                  {errors.endDate && <span className="field-error">{errors.endDate}</span>}
                </div>
              </div>
            </fieldset>

            <fieldset>
              <legend>Vendor Contact</legend>
              <div className="mode-tabs">
                <button type="button" className={`mode-tab${contactMode === 'existing' ? ' mode-tab-active' : ''}`} onClick={() => setContactMode('existing')}>
                  Select Existing
                </button>
                <button type="button" className={`mode-tab${contactMode === 'new' ? ' mode-tab-active' : ''}`} onClick={() => setContactMode('new')}>
                  Create New
                </button>
              </div>
              {contactMode === 'existing' ? (
                <div className="form-field">
                  <label>Vendor Contact</label>
                  <select value={selectedContactId} onChange={e => setSelectedContactId(e.target.value)}>
                    <option value="">— select a contact —</option>
                    {contacts.map(c => (
                      <option key={c.id} value={c.id}>{c.name} — {c.vendorName}</option>
                    ))}
                  </select>
                </div>
              ) : (
                <>
                  <div className="form-field">
                    <label>Vendor Name</label>
                    <input type="text" value={newContact.vendor} onChange={e => setNewContact({ ...newContact, vendor: e.target.value })} placeholder="e.g. Amazon Web Services" />
                  </div>
                  <div className="form-field">
                    <label>Contact Name</label>
                    <input type="text" value={newContact.contactName} onChange={e => setNewContact({ ...newContact, contactName: e.target.value })} placeholder="e.g. Support Team" />
                  </div>
                  <div className="form-field">
                    <label>Contact Email</label>
                    <input type="email" value={newContact.contactEmail} onChange={e => setNewContact({ ...newContact, contactEmail: e.target.value })} placeholder="e.g. support@vendor.com" />
                  </div>
                  <div className="form-field">
                    <label>Contact Phone</label>
                    <input type="text" value={newContact.contactPhone} onChange={e => setNewContact({ ...newContact, contactPhone: e.target.value })} placeholder="e.g. +1-800-555-0100" />
                  </div>
                </>
              )}
              {contactError && <p className="field-error">{contactError}</p>}
            </fieldset>

            <fieldset>
              <legend>Responsible Personnel</legend>
              <div className="mode-tabs">
                <button type="button" className={`mode-tab${employeeMode === 'existing' ? ' mode-tab-active' : ''}`} onClick={() => setEmployeeMode('existing')}>
                  Select Existing
                </button>
                <button type="button" className={`mode-tab${employeeMode === 'new' ? ' mode-tab-active' : ''}`} onClick={() => setEmployeeMode('new')}>
                  Create New
                </button>
              </div>
              {employeeMode === 'existing' ? (
                <div className="form-field">
                  <label>Select Employees</label>
                  <div className="employee-checklist-form">
                    {employees.length === 0 ? (
                      <p className="empty-hint">No employees found. Switch to "Create New" to add one.</p>
                    ) : employees.map(emp => (
                      <label key={emp.id} className="emp-check-label">
                        <input
                          type="checkbox"
                          checked={selectedEmployeeIds.includes(emp.id)}
                          onChange={() => toggleEmployee(emp.id)}
                        />
                        {emp.name} — {emp.department}
                      </label>
                    ))}
                  </div>
                </div>
              ) : (
                <>
                  <div className="form-field">
                    <label>Name</label>
                    <input type="text" value={newEmployee.empName} onChange={e => setNewEmployee({ ...newEmployee, empName: e.target.value })} placeholder="e.g. Alice Johnson" />
                  </div>
                  <div className="form-field">
                    <label>Email</label>
                    <input type="email" value={newEmployee.empEmail} onChange={e => setNewEmployee({ ...newEmployee, empEmail: e.target.value })} placeholder="e.g. alice@company.com" />
                  </div>
                  <div className="form-field">
                    <label>Department</label>
                    <input type="text" value={newEmployee.empDepartment} onChange={e => setNewEmployee({ ...newEmployee, empDepartment: e.target.value })} placeholder="e.g. IT Infrastructure" />
                  </div>
                </>
              )}
              {employeeError && <p className="field-error">{employeeError}</p>}
            </fieldset>

            {submitError && <p className="field-error">{submitError}</p>}

            <button type="submit" className="btn-primary btn-submit" disabled={isSubmitting}>
              {isSubmitting ? 'Registering...' : 'Register Service'}
            </button>
          </form>
        )}
      </main>
    </div>
  )
}
