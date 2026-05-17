import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'
export const tokenStorageKey = 'ptvs_auth_token'

export type UserInfo = {
  username: string
  roles: string[]
}

export function useAuth() {
  const navigate = useNavigate()
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem(tokenStorageKey)
    if (!token) {
      navigate('/login')
      return
    }
    fetch(`${apiBaseUrl}/auth/me`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then(r => {
        if (!r.ok) throw new Error()
        return r.json() as Promise<UserInfo>
      })
      .then(data => {
        setUserInfo(data)
        setIsLoading(false)
      })
      .catch(() => {
        localStorage.removeItem(tokenStorageKey)
        navigate('/login')
      })
  }, [navigate])

  function logout() {
    localStorage.removeItem(tokenStorageKey)
    navigate('/login')
  }

  return { userInfo, isLoading, logout }
}
