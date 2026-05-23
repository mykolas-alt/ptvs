import { useState, useEffect } from 'react'
import type { FormEvent } from 'react'
import { useAuth, tokenStorageKey } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import { OptimisticLockConflictModal } from '../components/OptimisticLockConflictModal'
import '../styles/ManagePage.css'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'

type VendorContact = {
  id: string
  name: string
  email: string
  phone: string
  address: string
  vendorName: string
  version: number
}

type ContactForm = {
  vendorName: string
  name: string
  email: string
  phone: string
}

type ContactErrors = Partial<Record<keyof ContactForm, string>>

function validate(f: ContactForm): ContactErrors {
  const e: ContactErrors = {}
  if (!f.vendorName.trim()) e.vendorName = 'Vendor name is required.'
  if (!f.name.trim()) e.name = 'Contact name is required.'
  if (!f.email.trim()) e.email = 'Email is required.'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(f.email)) e.email = 'Email is invalid.'
  if (!f.phone.trim()) e.phone = 'Phone is required.'
  return e
}

const EMPTY: ContactForm = { vendorName: '', name: '', email: '', phone: '' }

export function VendorContacts() {
  const { userInfo, isLoading, logout } = useAuth()
  const [contacts, setContacts] = useState<VendorContact[]>([])
  const [listLoading, setListLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [editingVersion, setEditingVersion] = useState<number | null>(null)
  const [form, setForm] = useState<ContactForm>(EMPTY)
  const [errors, setErrors] = useState<ContactErrors>({})
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [showConflict, setShowConflict] = useState(false)
  const [conflictLoading, setConflictLoading] = useState(false)

  function getToken() { return localStorage.getItem(tokenStorageKey) }

  useEffect(() => {
    if (isLoading) return
    fetch(`${apiBaseUrl}/vendor-contacts?size=10000`, {
      headers: { Authorization: `Bearer ${getToken()}` },
    })
      .then(r => r.ok ? r.json() as Promise<{ content: VendorContact[] }> : Promise.resolve({ content: [] as VendorContact[] }))
      .then(page => { setContacts(Array.isArray(page.content) ? page.content : []); setListLoading(false) })
      .catch(() => setListLoading(false))
  }, [isLoading])

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  function setField(field: keyof ContactForm, value: string) {
    setForm(prev => ({ ...prev, [field]: value }))
    if (errors[field]) setErrors(prev => ({ ...prev, [field]: undefined }))
  }

  function openCreate() {
    setEditingId(null)
    setForm(EMPTY)
    setErrors({})
    setSubmitError(null)
    setShowForm(true)
  }

  function openEdit(c: VendorContact) {
    setEditingId(c.id)
    setEditingVersion(c.version)
    setForm({ vendorName: c.vendorName, name: c.name, email: c.email, phone: c.phone ?? '' })
    setErrors({})
    setSubmitError(null)
    setShowForm(true)
  }

  function closeForm() {
    setShowForm(false)
    setEditingId(null)
    setEditingVersion(null)
    setForm(EMPTY)
    setErrors({})
  }

  async function submitUpdate(forceUpdate = false) {
    if (!editingId) return
    const token = getToken()
    const res = await fetch(`${apiBaseUrl}/vendor-contacts/${editingId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: JSON.stringify({ ...form, version: editingVersion, forceUpdate }),
    })
    if (res.status === 409) { setShowConflict(true); return }
    if (!res.ok) throw new Error('Failed to update contact.')
    const updated = await res.json() as VendorContact
    setContacts(prev => prev.map(c => c.id === updated.id ? updated : c))
    setShowConflict(false)
    closeForm()
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    const errs = validate(form)
    if (Object.keys(errs).length > 0) { setErrors(errs); return }

    setIsSubmitting(true)
    setSubmitError(null)
    try {
      if (editingId) {
        await submitUpdate()
      } else {
        const res = await fetch(`${apiBaseUrl}/vendor-contacts`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${getToken()}` },
          body: JSON.stringify(form),
        })
        if (!res.ok) throw new Error('Failed to create contact.')
        const created = await res.json() as VendorContact
        setContacts(prev => [created, ...prev])
        closeForm()
      }
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : 'Failed to save contact.')
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleConflictReload() {
    if (!editingId) return
    setConflictLoading(true)
    try {
      const res = await fetch(`${apiBaseUrl}/vendor-contacts/${editingId}`, {
        headers: { Authorization: `Bearer ${getToken()}` },
      })
      if (!res.ok) throw new Error()
      const fresh = await res.json() as VendorContact
      setContacts(prev => prev.map(c => c.id === fresh.id ? fresh : c))
      setEditingVersion(fresh.version)
      setForm({ vendorName: fresh.vendorName, name: fresh.name, email: fresh.email, phone: fresh.phone ?? '' })
      setShowConflict(false)
    } catch {
      setSubmitError('Failed to reload contact data.')
      setShowConflict(false)
    } finally {
      setConflictLoading(false)
    }
  }

  async function handleConflictForce() {
    setConflictLoading(true)
    try {
      await submitUpdate(true)
    } catch {
      setSubmitError('Force update failed.')
      setShowConflict(false)
    } finally {
      setConflictLoading(false)
    }
  }

  return (
    <div className="page-container">
      <Navbar userInfo={userInfo} onLogout={logout} />
      {showConflict && (
        <OptimisticLockConflictModal
          entityLabel="vendor contact"
          onReload={handleConflictReload}
          onForce={handleConflictForce}
          onClose={() => setShowConflict(false)}
          isLoading={conflictLoading}
        />
      )}
      <main className="manage-main">
        <div className="manage-toolbar">
          <h1>Vendor Contacts</h1>
          <button className="btn-primary btn-sm-add" onClick={showForm && !editingId ? closeForm : openCreate}>
            {showForm && !editingId ? 'Cancel' : '+ Add Contact'}
          </button>
        </div>

        {showForm && (
          <form className="manage-form" onSubmit={handleSubmit} noValidate>
            <p className="manage-form-title">{editingId ? 'Edit Contact' : 'New Contact'}</p>
            <div className="manage-form-grid">
              <div className={`form-field${errors.vendorName ? ' has-error' : ''}`}>
                <label>Vendor Name</label>
                <input type="text" value={form.vendorName} onChange={e => setField('vendorName', e.target.value)} placeholder="e.g. Amazon Web Services" />
                {errors.vendorName && <span className="field-error">{errors.vendorName}</span>}
              </div>
              <div className={`form-field${errors.name ? ' has-error' : ''}`}>
                <label>Contact Name</label>
                <input type="text" value={form.name} onChange={e => setField('name', e.target.value)} placeholder="e.g. Support Team" />
                {errors.name && <span className="field-error">{errors.name}</span>}
              </div>
              <div className={`form-field${errors.email ? ' has-error' : ''}`}>
                <label>Email</label>
                <input type="email" value={form.email} onChange={e => setField('email', e.target.value)} placeholder="e.g. support@vendor.com" />
                {errors.email && <span className="field-error">{errors.email}</span>}
              </div>
              <div className={`form-field${errors.phone ? ' has-error' : ''}`}>
                <label>Phone</label>
                <input type="text" value={form.phone} onChange={e => setField('phone', e.target.value)} placeholder="e.g. +1-800-555-0100" />
                {errors.phone && <span className="field-error">{errors.phone}</span>}
              </div>
            </div>
            {submitError && <p className="field-error">{submitError}</p>}
            <div className="manage-form-actions">
              <button type="submit" className="btn-primary btn-submit" disabled={isSubmitting}>
                {isSubmitting ? 'Saving...' : editingId ? 'Save Changes' : 'Save Contact'}
              </button>
              {editingId && (
                <button type="button" className="btn-cancel" onClick={closeForm}>Cancel</button>
              )}
            </div>
          </form>
        )}

        <div className="table-wrapper">
          {listLoading ? (
            <p className="loading-text">Loading contacts...</p>
          ) : (
            <table className="services-table">
              <thead>
                <tr>
                  <th>Vendor</th>
                  <th>Contact Name</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {contacts.length === 0 ? (
                  <tr><td colSpan={5} className="empty-row">No vendor contacts yet.</td></tr>
                ) : contacts.map(c => (
                  <tr key={c.id} className={editingId === c.id ? 'row-editing' : ''}>
                    <td>{c.vendorName}</td>
                    <td>{c.name}</td>
                    <td>{c.email}</td>
                    <td>{c.phone || '—'}</td>
                    <td className="action-cell">
                      <button
                        className="btn-row-edit"
                        onClick={() => editingId === c.id ? closeForm() : openEdit(c)}
                      >
                        {editingId === c.id ? 'Cancel' : 'Edit'}
                      </button>
                    </td>
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
