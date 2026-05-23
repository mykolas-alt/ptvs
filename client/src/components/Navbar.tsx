import { NavLink } from 'react-router-dom'
import type { UserInfo } from '../hooks/useAuth'
import '../styles/Navbar.css'

export type NavbarProps = {
  userInfo: UserInfo | null
  onLogout: () => void
}

export function Navbar({ userInfo, onLogout }: NavbarProps) {
  return (
    <nav className="navbar">
      <div className="navbar-brand">PTVS</div>
      <div className="navbar-links">
        <NavLink to="/dashboard" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Services
        </NavLink>
        <NavLink to="/services/register" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Register Service
        </NavLink>
        <NavLink to="/contacts" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Contacts
        </NavLink>
        <NavLink to="/employees" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Employees
        </NavLink>
        <NavLink to="/reports" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Reports
        </NavLink>
        <NavLink to="/notifications" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
          Notifications
        </NavLink>
        {userInfo?.roles.includes('ADMIN') && (
          <NavLink to="/accounts" className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
            Accounts
          </NavLink>
        )}
      </div>
      <div className="navbar-user">
        {userInfo && <span className="navbar-username">{userInfo.username}</span>}
        <button onClick={onLogout} className="logout-button">Logout</button>
      </div>
    </nav>
  )
}
