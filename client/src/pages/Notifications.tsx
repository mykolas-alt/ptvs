import { useState, useEffect } from 'react'
import { useAuth, tokenStorageKey } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import '../styles/Notifications.css'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'

type UserConfig = {
  id?: string
  notificationsEnabled: boolean
  notifyAllVendors: boolean
  daysBeforeExpiry: number | ''
  additionalEmails: string
}

type VendorConfigEntry = {
  id?: string
  vendorId: string
  vendorEnabled: boolean
  daysBeforeExpiry: number | ''
  additionalEmails: string
}

type VendorContact = {
  id: string
  name: string
  vendorName: string
}

const DEFAULT_CONFIG: UserConfig = {
  notificationsEnabled: false,
  notifyAllVendors: true,
  daysBeforeExpiry: 30,
  additionalEmails: '',
}

export function Notifications() {
  const { userInfo, isLoading, logout } = useAuth()

  const [config, setConfig] = useState<UserConfig>(DEFAULT_CONFIG)
  const [configExists, setConfigExists] = useState(false)
  const [configLoading, setConfigLoading] = useState(true)
  const [configSaving, setConfigSaving] = useState(false)
  const [configError, setConfigError] = useState<string | null>(null)
  const [configSaved, setConfigSaved] = useState(false)

  const [vendorConfigs, setVendorConfigs] = useState<VendorConfigEntry[]>([])
  const [allContacts, setAllContacts] = useState<VendorContact[]>([])

  const [addVendorId, setAddVendorId] = useState('')
  const [addVendorError, setAddVendorError] = useState<string | null>(null)
  const [vendorSaving, setVendorSaving] = useState(false)

  function getToken() { return localStorage.getItem(tokenStorageKey) }

  useEffect(() => {
    if (isLoading) return
    const token = getToken()
    const headers = { Authorization: `Bearer ${token}` }

    Promise.all([
      fetch(`${apiBaseUrl}/notifications/config`, { headers }),
      fetch(`${apiBaseUrl}/notifications/vendor-config`, { headers }),
      fetch(`${apiBaseUrl}/vendor-contacts`, { headers }),
    ]).then(async ([cfgRes, vcfgRes, contactsRes]) => {
      if (cfgRes.ok) {
        const data = await cfgRes.json() as UserConfig
        setConfig({ ...data, daysBeforeExpiry: data.daysBeforeExpiry ?? '' })
        setConfigExists(true)
      }
      if (vcfgRes.ok) {
        const data = await vcfgRes.json() as VendorConfigEntry[]
        setVendorConfigs(data.map(v => ({ ...v, daysBeforeExpiry: v.daysBeforeExpiry ?? '' })))
      }
      if (contactsRes.ok) {
        setAllContacts(await contactsRes.json() as VendorContact[])
      }
      setConfigLoading(false)
    }).catch(() => setConfigLoading(false))
  }, [isLoading])

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  async function saveGlobalConfig() {
    if (!config.daysBeforeExpiry || Number(config.daysBeforeExpiry) <= 0) {
      setConfigError('Days before expiry must be greater than 0.')
      return
    }
    setConfigSaving(true)
    setConfigError(null)
    setConfigSaved(false)
    const token = getToken()
    const method = configExists ? 'PUT' : 'POST'
    try {
      const res = await fetch(`${apiBaseUrl}/notifications/config`, {
        method,
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({
          notificationsEnabled: config.notificationsEnabled,
          notifyAllVendors: config.notifyAllVendors,
          daysBeforeExpiry: Number(config.daysBeforeExpiry),
          additionalEmails: config.additionalEmails,
        }),
      })
      if (!res.ok) throw new Error('Failed to save configuration.')
      const saved = await res.json() as UserConfig
      setConfig({ ...saved, daysBeforeExpiry: saved.daysBeforeExpiry ?? '' })
      setConfigExists(true)
      setConfigSaved(true)
      setTimeout(() => setConfigSaved(false), 3000)
    } catch (err) {
      setConfigError(err instanceof Error ? err.message : 'Save failed.')
    } finally {
      setConfigSaving(false)
    }
  }

  async function addVendorConfig() {
    if (!addVendorId) { setAddVendorError('Please select a vendor.'); return }
    if (vendorConfigs.some(v => v.vendorId === addVendorId)) { setAddVendorError('Config for this vendor already exists.'); return }
    setAddVendorError(null)
    setVendorSaving(true)
    const token = getToken()
    try {
      const res = await fetch(`${apiBaseUrl}/notifications/vendor-config`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ vendorId: addVendorId, vendorEnabled: true, daysBeforeExpiry: null, additionalEmails: '' }),
      })
      if (!res.ok) throw new Error('Failed to add vendor config.')
      const created = await res.json() as VendorConfigEntry
      setVendorConfigs(prev => [...prev, { ...created, daysBeforeExpiry: created.daysBeforeExpiry ?? '' }])
      setAddVendorId('')
    } catch (err) {
      setAddVendorError(err instanceof Error ? err.message : 'Failed to add.')
    } finally {
      setVendorSaving(false)
    }
  }

  async function updateVendorConfig(entry: VendorConfigEntry) {
    const token = getToken()
    try {
      const res = await fetch(`${apiBaseUrl}/notifications/vendor-config/${entry.vendorId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({
          vendorId: entry.vendorId,
          vendorEnabled: entry.vendorEnabled,
          daysBeforeExpiry: entry.daysBeforeExpiry === '' ? null : Number(entry.daysBeforeExpiry),
          additionalEmails: entry.additionalEmails,
        }),
      })
      if (!res.ok) throw new Error()
      const updated = await res.json() as VendorConfigEntry
      setVendorConfigs(prev => prev.map(v => v.vendorId === entry.vendorId ? { ...updated, daysBeforeExpiry: updated.daysBeforeExpiry ?? '' } : v))
    } catch {
      alert('Failed to update vendor configuration.')
    }
  }

  async function deleteVendorConfig(vendorId: string) {
    const token = getToken()
    try {
      const res = await fetch(`${apiBaseUrl}/notifications/vendor-config/${vendorId}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      })
      if (!res.ok) throw new Error()
      setVendorConfigs(prev => prev.filter(v => v.vendorId !== vendorId))
    } catch {
      alert('Failed to delete vendor configuration.')
    }
  }

  function patchVendorEntry(vendorId: string, patch: Partial<VendorConfigEntry>) {
    setVendorConfigs(prev => prev.map(v => v.vendorId === vendorId ? { ...v, ...patch } : v))
  }

  function vendorLabel(vendorId: string) {
    const c = allContacts.find(c => c.id === vendorId)
    return c ? `${c.name} (${c.vendorName})` : vendorId
  }

  return (
    <div className="page-container">
      <Navbar userInfo={userInfo} onLogout={logout} />
      <main className="notifications-main">
        <h1>Notification Settings</h1>

        {configLoading ? (
          <p className="loading-text">Loading settings...</p>
        ) : (
          <>
            <section className="notif-card">
              <h2>Global Configuration</h2>

              <div className="notif-toggle-row">
                <label className="notif-toggle-label">
                  <input
                    type="checkbox"
                    checked={config.notificationsEnabled}
                    onChange={e => setConfig(c => ({ ...c, notificationsEnabled: e.target.checked }))}
                  />
                  Enable notifications
                </label>
              </div>

              <div className="notif-toggle-row">
                <label className="notif-toggle-label">
                  <input
                    type="checkbox"
                    checked={config.notifyAllVendors}
                    onChange={e => setConfig(c => ({ ...c, notifyAllVendors: e.target.checked }))}
                    disabled={!config.notificationsEnabled}
                  />
                  Notify for all vendors
                </label>
                <span className="notif-hint">If off, only vendors listed below will trigger notifications.</span>
              </div>

              <div className="notif-field">
                <label>Days before expiry (default)</label>
                <input
                  type="number"
                  min="1"
                  className="notif-input-sm"
                  value={config.daysBeforeExpiry}
                  onChange={e => setConfig(c => ({ ...c, daysBeforeExpiry: e.target.value === '' ? '' : Number(e.target.value) }))}
                  disabled={!config.notificationsEnabled}
                />
              </div>

              <div className="notif-field">
                <label>Additional email recipients (comma-separated)</label>
                <input
                  type="text"
                  className="notif-input"
                  value={config.additionalEmails}
                  onChange={e => setConfig(c => ({ ...c, additionalEmails: e.target.value }))}
                  placeholder="e.g. manager@company.com, it@company.com"
                  disabled={!config.notificationsEnabled}
                />
              </div>

              {configError && <p className="notif-error">{configError}</p>}
              {configSaved && <p className="notif-success">Settings saved.</p>}

              <button className="btn-primary btn-save" onClick={saveGlobalConfig} disabled={configSaving}>
                {configSaving ? 'Saving…' : 'Save Settings'}
              </button>
            </section>

            {!config.notifyAllVendors && config.notificationsEnabled && (
              <section className="notif-card">
                <h2>Per-Vendor Configuration</h2>
                <p className="notif-hint notif-hint-block">
                  Override notification settings for specific vendors. Leave days/emails blank to inherit the global defaults.
                </p>

                <div className="vendor-add-row">
                  <select
                    value={addVendorId}
                    onChange={e => setAddVendorId(e.target.value)}
                    className="notif-select"
                  >
                    <option value="">— select vendor contact —</option>
                    {allContacts
                      .filter(c => !vendorConfigs.some(v => v.vendorId === c.id))
                      .map(c => (
                        <option key={c.id} value={c.id}>{c.name} ({c.vendorName})</option>
                      ))}
                  </select>
                  <button className="btn-primary btn-sm-add" onClick={addVendorConfig} disabled={vendorSaving}>
                    Add
                  </button>
                </div>
                {addVendorError && <p className="notif-error">{addVendorError}</p>}

                {vendorConfigs.length > 0 && (
                  <div className="vendor-config-list">
                    {vendorConfigs.map(entry => (
                      <div key={entry.vendorId} className="vendor-config-row">
                        <div className="vendor-config-name">{vendorLabel(entry.vendorId)}</div>
                        <label className="notif-toggle-label">
                          <input
                            type="checkbox"
                            checked={entry.vendorEnabled}
                            onChange={e => patchVendorEntry(entry.vendorId, { vendorEnabled: e.target.checked })}
                          />
                          Enabled
                        </label>
                        <div className="vendor-config-field">
                          <label>Days</label>
                          <input
                            type="number"
                            min="1"
                            className="notif-input-sm"
                            value={entry.daysBeforeExpiry}
                            onChange={e => patchVendorEntry(entry.vendorId, { daysBeforeExpiry: e.target.value === '' ? '' : Number(e.target.value) })}
                            placeholder="global"
                          />
                        </div>
                        <div className="vendor-config-field vendor-config-emails">
                          <label>Additional emails</label>
                          <input
                            type="text"
                            className="notif-input"
                            value={entry.additionalEmails}
                            onChange={e => patchVendorEntry(entry.vendorId, { additionalEmails: e.target.value })}
                            placeholder="inherit from global"
                          />
                        </div>
                        <div className="vendor-config-actions">
                          <button className="btn-secondary btn-sm" onClick={() => updateVendorConfig(entry)}>Save</button>
                          <button className="btn-danger btn-sm" onClick={() => deleteVendorConfig(entry.vendorId)}>Remove</button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </section>
            )}
          </>
        )}
      </main>
    </div>
  )
}
