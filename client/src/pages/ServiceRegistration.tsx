import { useState } from 'react'
import type { FormEvent, ReactNode } from 'react'
import { useAuth, tokenStorageKey } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import '../styles/ServiceRegistration.css'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'

type FormData = {
  name: string
  vendor: string
  price: string
  startDate: string
  endDate: string
  contactName: string
  contactEmail: string
  contactPhone: string
  responsibleName: string
  responsibleEmail: string
  responsibleDepartment: string
}

type Errors = Partial<Record<keyof FormData, string>>

type FieldProps = {
  label: string
  error?: string
  children: ReactNode
}

function Field({ label, error, children }: FieldProps) {
  return (
    <div className={`form-field${error ? ' has-error' : ''}`}>
      <label>{label}</label>
      {children}
      {error && <span className="field-error">{error}</span>}
    </div>
  )
}

const EMPTY: FormData = {
  name: '',
  vendor: '',
  price: '',
  startDate: '',
  endDate: '',
  contactName: '',
  contactEmail: '',
  contactPhone: '',
  responsibleName: '',
  responsibleEmail: '',
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
  if (!data.contactName.trim()) e.contactName = 'Contact name is required.'
  if (!data.contactEmail.trim()) {
    e.contactEmail = 'Contact email is required.'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.contactEmail)) {
    e.contactEmail = 'Contact email is invalid.'
  }
  if (!data.contactPhone.trim()) e.contactPhone = 'Contact phone is required.'
  if (!data.responsibleName.trim()) e.responsibleName = 'Responsible person name is required.'
  if (!data.responsibleEmail.trim()) {
    e.responsibleEmail = 'Responsible person email is required.'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.responsibleEmail)) {
    e.responsibleEmail = 'Responsible person email is invalid.'
  }
  if (!data.responsibleDepartment.trim()) e.responsibleDepartment = 'Department is required.'
  return e
}

export function ServiceRegistration() {
  const { userInfo, isLoading, logout } = useAuth()
  const [form, setForm] = useState<FormData>(EMPTY)
  const [errors, setErrors] = useState<Errors>({})
  const [submitted, setSubmitted] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  function set(field: keyof FormData, value: string) {
    setForm(prev => ({ ...prev, [field]: value }))
    if (errors[field]) setErrors(prev => ({ ...prev, [field]: undefined }))
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    const errs = validate(form)
    if (Object.keys(errs).length > 0) {
      setErrors(errs)
      return
    }

    setIsSubmitting(true)
    setSubmitError(null)

    const token = localStorage.getItem(tokenStorageKey)
    const headers = {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    }

    try {
      const vcRes = await fetch(`${apiBaseUrl}/vendor-contacts`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          name: form.contactName,
          email: form.contactEmail,
          phone: form.contactPhone,
          vendorName: form.vendor,
        }),
      })
      if (!vcRes.ok) throw new Error('Failed to create vendor contact.')
      const vc = await vcRes.json() as { id: string }

      const empRes = await fetch(`${apiBaseUrl}/employees`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          name: form.responsibleName,
          email: form.responsibleEmail,
          department: form.responsibleDepartment,
        }),
      })
      if (!empRes.ok) throw new Error('Failed to create employee record.')
      const emp = await empRes.json() as { id: string }

      const svcRes = await fetch(`${apiBaseUrl}/services`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          serviceName: form.name,
          monthlyCost: Number(form.price),
          contractStartDate: form.startDate,
          contractEndDate: form.endDate,
          vendorContactId: vc.id,
          responsiblePersonnelIds: [emp.id],
        }),
      })
      if (!svcRes.ok) throw new Error('Failed to register service.')

      setSubmitted(true)
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Registration failed.')
    } finally {
      setIsSubmitting(false)
    }
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
              onClick={() => { setForm(EMPTY); setErrors({}); setSubmitted(false); setSubmitError(null) }}
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
              <Field label="Responsible Person Email" error={errors.responsibleEmail}>
                <input
                  type="email"
                  value={form.responsibleEmail}
                  onChange={e => set('responsibleEmail', e.target.value)}
                  placeholder="e.g. alice@company.com"
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
