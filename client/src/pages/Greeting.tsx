import { useNavigate } from 'react-router-dom'
import '../styles/Greeting.css'

export function Greeting() {
  const navigate = useNavigate()

  return (
    <div className="greeting-container">
      <div className="greeting-content">
        <h1 className="greeting-title">Welcome to PTVS</h1>
        <p className="greeting-subtitle">
          Your secure authentication system
        </p>
        <button
          className="greeting-button"
          onClick={() => navigate('/login')}
        >
          Sign In
        </button>
      </div>
    </div>
  )
}
