import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type UserInfo = {
  username: string
  roles: string[]
}

type LoginResponse = {
  token: string
}

type AuthMode = 'login' | 'register'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'
const tokenStorageKey = 'ptvs_auth_token'

function App() {
  const [username, setUsername] = useState('ADMIN')
  const [password, setPassword] = useState('ADMIN')
  const [mode, setMode] = useState<AuthMode>('login')
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(tokenStorageKey))
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [message, setMessage] = useState<string | null>(null)

  useEffect(() => {
    if (!token) {
      return
    }
    void loadMe(token)
  }, [token])

  async function loadMe(currentToken: string) {
    setIsLoading(true)
    setError(null)
    setMessage(null)
    try {
      const response = await fetch(`${apiBaseUrl}/auth/me`, {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${currentToken}`,
        },
      })

      if (!response.ok) {
        throw new Error('Session is not valid.')
      }

      const data = (await response.json()) as UserInfo
      setUserInfo(data)
    } catch {
      localStorage.removeItem(tokenStorageKey)
      setToken(null)
      setUserInfo(null)
      setError('Session expired. Please log in again.')
    } finally {
      setIsLoading(false)
    }
  }

  async function handleAuth(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsLoading(true)
    setError(null)
    setMessage(null)

    try {
      const endpoint = mode === 'login' ? 'login' : 'register'
      const response = await fetch(`${apiBaseUrl}/auth/${endpoint}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
      })

      if (!response.ok) {
        if (mode === 'register' && response.status === 409) {
          throw new Error('Username already exists.')
        }
        throw new Error('Authentication failed.')
      }

      const data = (await response.json()) as LoginResponse
      localStorage.setItem(tokenStorageKey, data.token)
      setToken(data.token)
      await loadMe(data.token)
      if (mode === 'register') {
        setMessage('Registration successful. Default USER role assigned.')
      }
    } catch (caughtError) {
      if (caughtError instanceof Error) {
        setError(caughtError.message)
      } else {
        setError('Authentication failed.')
      }
    } finally {
      setIsLoading(false)
    }
  }

  function handleLogout() {
    localStorage.removeItem(tokenStorageKey)
    setToken(null)
    setUserInfo(null)
    setError(null)
    setMessage(null)
    setMode('login')
  }

  async function handleAdminPing() {
    if (!token) {
      return
    }

    setError(null)
    setMessage(null)
    const response = await fetch(`${apiBaseUrl}/admin/ping`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })

    if (!response.ok) {
      setError('Admin endpoint call failed.')
      return
    }

    const text = await response.text()
    setMessage(`Admin endpoint response: ${text}`)
  }

  return (
    <main className="container">
      <section className="card">
        <h1>{mode === 'login' ? 'PTVS Login' : 'PTVS Register'}</h1>
        <p className="muted">
          Test admin account: <strong>ADMIN / ADMIN</strong>
        </p>

        {!userInfo ? (
          <>
            <div className="mode-switch">
              <button
                type="button"
                className={mode === 'login' ? 'mode-active' : 'secondary'}
                onClick={() => setMode('login')}
              >
                Login
              </button>
              <button
                type="button"
                className={mode === 'register' ? 'mode-active' : 'secondary'}
                onClick={() => setMode('register')}
              >
                Register
              </button>
            </div>
            <form className="form" onSubmit={handleAuth}>
              <label>
                Username
                <input value={username} onChange={(event) => setUsername(event.target.value)} required />
              </label>
              <label>
                Password
                <input
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  required
                />
              </label>
              <button type="submit" disabled={isLoading}>
                {isLoading ? 'Please wait...' : mode === 'login' ? 'Login' : 'Register'}
              </button>
            </form>
          </>
        ) : (
          <div className="session">
            <p>
              Logged in as <strong>{userInfo.username}</strong> ({userInfo.roles.join(', ')})
            </p>
            <div className="row">
              <button type="button" onClick={handleAdminPing}>
                Call admin endpoint
              </button>
              <button type="button" className="secondary" onClick={handleLogout}>
                Logout
              </button>
            </div>
          </div>
        )}

        {error && <p className="error">{error}</p>}
        {message && <p className="success">{message}</p>}
      </section>
    </main>
  )
}

export default App
