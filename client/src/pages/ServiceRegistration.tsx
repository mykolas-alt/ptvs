import { useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import { useAuth } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import '../styles/ServiceRegistration.css'

type Status = 'active' | 'pending' | 'ended'

type FormData = {
  name: string
  vendor: string
  price: string
  startDate: string
  endDate: string
  status: Status | ''
  contactName: string
  contactEmail: string
  contactPhone: string
  responsibleName: string
  responsibleDepartment: string
}

type Errors = Partial<Record<keyof FormData, string>>

const EMPTY: FormData = {
  name: '',
  vendor: '',
  price: '',
  startDate: '',
  endDate: '',
  status: '',
  contactName: '',
  contactEmail: '',
  contactPhone: '',
  responsibleName: '',
  responsibleDepartment: '',
}

function validate(data: FormData): Errors {
  const e: Errors = {}
  if (!data.name.trim()) e.name = 'Service name is required.'
  if (!data.vendor.trim()) e.vendor = 'Vendor is required.'
  if (!data.price) {
    e.price = 'Monthly cost is required.'
  } else if (Number(data.price) <= 0) {
    e.price = 'Monthly cost must be greater than 0.'
  }
  if (!data.startDate) e.startDate = 'Start date is required.'
  if (!data.endDate) {
    e.endDate = 'End date is required.'
  } else if (data.startDate && data.endDate <= data.startDate) {
    e.endDate = 'End date must be after start date.'
  }
  if (!data.status) e.status = 'Status is required.'
  if (!data.contactName.trim()) e.contactName = 'Contact name is required.'
  if (!data.contactEmail.trim()) {
    e.contactEmail = 'Contact email is required.'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.contactEmail)) {
    e.contactEmail = 'Contact email is invalid.'
  }
  if (!data.contactPhone.trim()) e.contactPhone = 'Contact phone is required.'
  if (!data.responsibleName.trim()) e.responsibleName = 'Responsible person name is required.'
  if (!data.responsibleDepartment.trim()) e.responsibleDepartment = 'Department is required.'
  return e
}

export function ServiceRegistration() {
  const { userInfo, isLoading, logout } = useAuth()
  const [form, setForm] = useState<FormData>(EMPTY)
  const [errors, setErrors] = useState<Errors>({})
  const [submitted, setSubmitted] = useState(false)

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  function set(field: keyof FormData, value: string) {
    setForm(prev => ({ ...prev, [field]: value }))
    if (errors[field]) setErrors(prev => ({ ...prev, [field]: undefined }))
  }

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    const errs = validate(form)
    if (Object.keys(errs).length > 0) {
      setErrors(errs)
      return
    }
    setSubmitted(true)
  }

  return (
    <div className="page-container">
      <Navbar userInfo={userInfo} onLogout={logout} />
      <main className="form-main">
        <h1>Register Third-Party Service</h1>

        {submitted ? (
          <div className="success-card">
            <h2>Service Registered</h2>
            <p>
              <strong>{form.name}</strong> by {form.vendor} has been successfully registered.
            </p>
            <button
              className="btn-primary"
              onClick={() => { setForm(EMPTY); setErrors({}); setSubmitted(false) }}
            >
              Register Another
            </button>
          </div>
        ) : (
          <form className="service-form" onSubmit={handleSubmit} noValidate>
            <fieldset>
              <legend>Service Details</legend>
              <Field label="Service Name" error={errors.name}>
                <input
                  type="text"
                  value={form.name}
                  onChange={e => set('name', e.target.value)}
                  placeholder="e.g. AWS Cloud"
                />
              </Field>
              <Field label="Vendor" error={errors.vendor}>
                <input
                  type="text"
                  value={form.vendor}
                  onChange={e => set('vendor', e.target.value)}
                  placeholder="e.g. Amazon Web Services"
                />
              </Field>
              <Field label="Monthly Cost (€)" error={errors.price}>
                <input
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={form.price}
                  onChange={e => set('price', e.target.value)}
                  placeholder="0.00"
                />
              </Field>
              <div className="field-row">
                <Field label="Contract Start Date" error={errors.startDate}>
                  <input
                    type="date"
                    value={form.startDate}
                    onChange={e => set('startDate', e.target.value)}
                  />
                </Field>
                <Field label="Contract End Date" error={errors.endDate}>
                  <input
                    type="date"
                    value={form.endDate}
                    onChange={e => set('endDate', e.target.value)}
                  />
                </Field>
              </div>
              <Field label="Status" error={errors.status}>
                <select
                  value={form.status}
                  onChange={e => set('status', e.target.value)}
                  className={form.status === '' ? 'select-placeholder' : ''}
                >
                  <option value="" disabled>Select status</option>
                  <option value="active">Active</option>
                  <option value="pending">Pending</option>
                  <option value="ended">Ended</option>
                </select>
              </Field>
            </fieldset>

            <fieldset>
              <legend>Contact Information</legend>
              <Field label="Contact Name" error={errors.contactName}>
                <input
                  type="text"
                  value={form.contactName}
                  onChange={e => set('contactName', e.target.value)}
                  placeholder="e.g. Support Team"
                />
              </Field>
              <Field label="Contact Email" error={errors.contactEmail}>
                <input
                  type="email"
                  value={form.contactEmail}
                  onChange={e => set('contactEmail', e.target.value)}
                  placeholder="e.g. support@vendor.com"
                />
              </Field>
              <Field label="Contact Phone" error={errors.contactPhone}>
                <input
                  type="text"
                  value={form.contactPhone}
                  onChange={e => set('contactPhone', e.target.value)}
                  placeholder="e.g. +1-800-555-0100"
                />
              </Field>
            </fieldset>

            <fieldset>
              <legend>Responsible Personnel</legend>
              <Field label="Responsible Person" error={errors.responsibleName}>
                <input
                  type="text"
                  value={form.responsibleName}
                  onChange={e => set('responsibleName', e.target.value)}
                  placeholder="e.g. Alice Johnson"
                />
              </Field>
              <Field label="Department" error={errors.responsibleDepartment}>
                <input
                  type="text"
                  value={form.responsibleDepartment}
                  onChange={e => set('responsibleDepartment', e.target.value)}
                  placeholder="e.g. IT Infrastructure"
                />
              </Field>
            </fieldset>

            <button type="submit" className="btn-primary btn-submit">
              Register Service
            </button>
          </form>
        )}
      </main>
    </div>
  )
}

function Field({
  label,
  error,
  children,
}: {
  label: string
  error?: string
  children: ReactNode
}) {
  return (
    <div className={`form-field${error ? ' has-error' : ''}`}>
      <label>{label}</label>
      {children}
      {error && <span className="field-error">{error}</span>}
    </div>
  )
}
