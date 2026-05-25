import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import '../styles/Auth.css'

type LoginResponse = {
  token: string
}

type AuthMode = 'login' | 'register'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'
const tokenStorageKey = 'ptvs_auth_token'

function Login() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [mode, setMode] = useState<AuthMode>('login')
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [message, setMessage] = useState<string | null>(null)

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

      if (mode === 'register') {
        setMessage('Registration successful. Default USER role assigned.')
        setMode('login')
        setUsername('')
        setPassword('')
      } else {
        navigate('/dashboard')
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

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-mode-switch">
          <button
            type="button"
            className={mode === 'login' ? 'mode-active' : 'mode-secondary'}
            onClick={() => {
              setMode('login')
              setError(null)
              setMessage(null)
            }}
            disabled={isLoading}
          >
            Sign In
          </button>
          <button
            type="button"
            className={mode === 'register' ? 'mode-active' : 'mode-secondary'}
            onClick={() => {
              setMode('register')
              setError(null)
              setMessage(null)
            }}
            disabled={isLoading}
          >
            Register
          </button>
        </div>
        <h1>{mode === 'login' ? 'Sign In' : 'Register'}</h1>
        <p className="auth-subtitle">
          {mode === 'login' ? 'Welcome back' : 'Create an account'}
        </p>

        <form onSubmit={handleAuth} className="auth-form">
          <label>
            Username
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={isLoading}
              required
            />
          </label>

          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              disabled={isLoading}
              required
            />
          </label>

          <button type="submit" disabled={isLoading} className="auth-button">
            {isLoading ? 'Loading...' : mode === 'login' ? 'Sign In' : 'Register'}
          </button>
        </form>

        

        {error && <div className="auth-error">{error}</div>}
        {message && <div className="auth-success">{message}</div>}
      </div>
    </div>
  )
}

export { Login }
