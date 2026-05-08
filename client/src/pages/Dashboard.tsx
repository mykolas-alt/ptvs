import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import '../styles/Dashboard.css'

type UserInfo = {
  username: string
  roles: string[]
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'
const tokenStorageKey = 'ptvs_auth_token'

export function Dashboard() {
  const navigate = useNavigate()
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem(tokenStorageKey)
    if (!token) {
      navigate('/login')
      return
    }
    loadMe(token)
  }, [navigate])

  async function loadMe(token: string) {
    setIsLoading(true)
    setError(null)
    try {
      console.log('Calling /auth/me with token:', token.substring(0, 20) + '...')
      const response = await fetch(`${apiBaseUrl}/auth/me`, {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })

      console.log('Response status:', response.status)

      if (!response.ok) {
        throw new Error('Session is not valid.')
      }

      const data = (await response.json()) as UserInfo
      console.log('User info:', data)
      setUserInfo(data)
    } catch (err) {
      console.error('Error loading user:', err)
      localStorage.removeItem(tokenStorageKey)
      navigate('/login')
      setError('Session expired. Please log in again.')
    } finally {
      setIsLoading(false)
    }
  }

  function handleLogout() {
    localStorage.removeItem(tokenStorageKey)
    navigate('/login')
  }

  if (isLoading) {
    return (
      <div className="dashboard-container">
        <p>Loading...</p>
      </div>
    )
  }

  if (!userInfo) {
    return (
      <div className="dashboard-container">
        <p>{error || 'Failed to load user info'}</p>
      </div>
    )
  }

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Dashboard</h1>
        <button onClick={handleLogout} className="logout-button">
          Logout
        </button>
      </div>

      <div className="dashboard-card">
        <h2>User Information</h2>
        <div className="user-info">
          <p>
            <strong>Username:</strong> {userInfo.username}
          </p>
          <p>
            <strong>Roles:</strong> {userInfo.roles.join(', ')}
          </p>
        </div>
      </div>
    </div>
  )
}
