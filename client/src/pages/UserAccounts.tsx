import { useState, useEffect } from 'react'
import { useAuth, tokenStorageKey } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import '../styles/ManagePage.css'
import '../styles/UserAccounts.css'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'

type UserAccount = {
  userId: string
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

  function getToken() { return localStorage.getItem(tokenStorageKey) }

  async function reloadUsers(): Promise<UserAccount[]> {
    const res = await fetch(`${apiBaseUrl}/admin/users`, {
      headers: { Authorization: `Bearer ${getToken()}` },
    })
    if (!res.ok) throw new Error()
    const data = await res.json() as UserAccount[] | { content: UserAccount[] }
    const list = Array.isArray(data) ? data : data.content
    return Array.isArray(list) ? list : []
  }

  useEffect(() => {
    if (isLoading) return
    reloadUsers()
      .then(list => { setUsers(list); setListLoading(false) })
      .catch(() => { setListError('Could not load user accounts.'); setListLoading(false) })
  }, [isLoading])

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  async function submitRoleUpdate(user: UserAccount, roles: string[]) {
    const res = await fetch(`${apiBaseUrl}/admin/users/${user.userId}/roles`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${getToken()}` },
      body: JSON.stringify({ roles, version: user.version, forceUpdate: false }),
    })
    if (res.status === 409) {
      const fresh = await reloadUsers()
      setUsers(fresh)
      const freshUser = fresh.find(u => u.userId === user.userId)
      if (freshUser) {
        const retryRes = await fetch(`${apiBaseUrl}/admin/users/${freshUser.userId}/roles`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${getToken()}` },
          body: JSON.stringify({ roles, version: freshUser.version }),
        })
        if (!retryRes.ok) throw new Error()
        const updated = await retryRes.json() as UserAccount
        setUsers(prev => prev.map(u => u.userId === updated.userId ? updated : u))
      }
      return
    }
    if (!res.ok) throw new Error()
    const updated = await res.json() as UserAccount
    setUsers(prev => prev.map(u => u.userId === updated.userId ? updated : u))
  }

  async function toggleAdmin(user: UserAccount) {
    const newRoles = user.roles.includes('ADMIN')
      ? user.roles.filter(r => r !== 'ADMIN')
      : [...user.roles, 'ADMIN']
    setTogglingId(user.userId)
    try {
      await submitRoleUpdate(user, newRoles)
    } catch {
      alert('Failed to update user roles.')
    } finally {
      setTogglingId(null)
    }
  }

  return (
    <div className="page-container">
      <Navbar userInfo={userInfo} onLogout={logout} />
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
                  const isToggling = togglingId === u.userId
                  return (
                    <tr key={u.userId}>
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
                        {!isSelf && (
                          <button
                            className={`btn-row-edit${isAdmin ? ' btn-row-revoke' : ' btn-row-grant'}`}
                            onClick={() => toggleAdmin(u)}
                            disabled={isToggling}
                          >
                            {isToggling ? '...' : isAdmin ? 'Revoke Admin' : 'Grant Admin'}
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
