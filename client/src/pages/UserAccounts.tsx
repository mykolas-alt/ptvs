import { useState, useEffect } from 'react'
import { useAuth, tokenStorageKey } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import { OptimisticLockConflictModal } from '../components/OptimisticLockConflictModal'
import '../styles/ManagePage.css'
import '../styles/UserAccounts.css'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'

type UserAccount = {
  id: string
  username: string
  roles: string[]
  version: number
}

export function UserAccounts() {
  const { userInfo, isLoading, logout } = useAuth()
  const [users, setUsers] = useState<UserAccount[]>([])
  const [listLoading, setListLoading] = useState(true)
  const [listError, setListError] = useState<string | null>(null)
  const [togglingId, setTogglingId] = useState<string | null>(null)
  const [showConflict, setShowConflict] = useState(false)
  const [conflictLoading, setConflictLoading] = useState(false)
  const [pendingUser, setPendingUser] = useState<UserAccount | null>(null)
  const [pendingRoles, setPendingRoles] = useState<string[]>([])

  function getToken() { return localStorage.getItem(tokenStorageKey) }

  useEffect(() => {
    if (isLoading) return
    fetch(`${apiBaseUrl}/admin/users`, {
      headers: { Authorization: `Bearer ${getToken()}` },
    })
      .then(r => { if (!r.ok) throw new Error(); return r.json() as Promise<UserAccount[] | { content: UserAccount[] }> })
      .then(data => {
        const list = Array.isArray(data) ? data : data.content
        setUsers(Array.isArray(list) ? list : [])
        setListLoading(false)
      })
      .catch(() => { setListError('Could not load user accounts.'); setListLoading(false) })
  }, [isLoading])

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  async function submitRoleUpdate(user: UserAccount, roles: string[], forceUpdate = false) {
    const res = await fetch(`${apiBaseUrl}/admin/users/${user.id}/roles`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${getToken()}` },
      body: JSON.stringify({ roles, version: user.version, forceUpdate }),
    })
    if (res.status === 409) {
      setPendingUser(user)
      setPendingRoles(roles)
      setShowConflict(true)
      return
    }
    if (!res.ok) throw new Error()
    const updated = await res.json() as UserAccount
    setUsers(prev => prev.map(u => u.id === updated.id ? updated : u))
    setShowConflict(false)
  }

  async function revokeAdmin(user: UserAccount) {
    const newRoles = user.roles.filter(r => r !== 'ADMIN')
    setTogglingId(user.id)
    try {
      await submitRoleUpdate(user, newRoles)
    } catch {
      alert('Failed to update user roles.')
    } finally {
      setTogglingId(null)
    }
  }

  async function handleConflictReload() {
    if (!pendingUser) return
    setConflictLoading(true)
    try {
      const res = await fetch(`${apiBaseUrl}/admin/users`, {
        headers: { Authorization: `Bearer ${getToken()}` },
      })
      if (!res.ok) throw new Error()
      const data = await res.json() as UserAccount[] | { content: UserAccount[] }
      const list = Array.isArray(data) ? data : data.content
      setUsers(Array.isArray(list) ? list : [])
      setShowConflict(false)
    } catch {
      alert('Failed to reload user data.')
      setShowConflict(false)
    } finally {
      setConflictLoading(false)
    }
  }

  async function handleConflictForce() {
    if (!pendingUser) return
    setConflictLoading(true)
    try {
      await submitRoleUpdate(pendingUser, pendingRoles, true)
    } catch {
      alert('Force update failed.')
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
          entityLabel="user account"
          onReload={handleConflictReload}
          onForce={handleConflictForce}
          onClose={() => setShowConflict(false)}
          isLoading={conflictLoading}
        />
      )}
      <main className="manage-main">
        <div className="manage-toolbar">
          <h1>User Accounts</h1>
        </div>

        <div className="table-wrapper">
          {listLoading ? (
            <p className="loading-text">Loading users...</p>
          ) : listError ? (
            <p className="loading-text">{listError}</p>
          ) : (
            <table className="services-table">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Roles</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {users.length === 0 ? (
                  <tr><td colSpan={3} className="empty-row">No user accounts found.</td></tr>
                ) : users.map(u => {
                  const isAdmin = u.roles.includes('ADMIN')
                  const isSelf = u.username === userInfo?.username
                  const isToggling = togglingId === u.id
                  return (
                    <tr key={u.id}>
                      <td>
                        {u.username}
                        {isSelf && <span className="self-badge">you</span>}
                      </td>
                      <td>
                        <div className="role-badges">
                          {u.roles.map(r => (
                            <span key={r} className={`role-badge role-${r.toLowerCase()}`}>{r}</span>
                          ))}
                        </div>
                      </td>
                      <td className="action-cell">
                        {!isSelf && isAdmin && (
                          <button
                            className="btn-row-edit btn-row-revoke"
                            onClick={() => revokeAdmin(u)}
                            disabled={isToggling}
                          >
                            {isToggling ? '...' : 'Revoke Admin'}
                          </button>
                        )}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          )}
        </div>
      </main>
    </div>
  )
}
