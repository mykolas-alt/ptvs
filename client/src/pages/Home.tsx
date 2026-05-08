import { Navigate } from 'react-router-dom'

const tokenStorageKey = 'ptvs_auth_token'

export function Home() {
  const token = localStorage.getItem(tokenStorageKey)

  if (token) {
    return <Navigate to="/dashboard" replace />
  }

  return <Navigate to="/login" replace />
}
