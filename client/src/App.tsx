import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { Home } from './pages/Home'
import { Login } from './pages/Login'
import { Dashboard } from './pages/Dashboard'
import { ServiceRegistration } from './pages/ServiceRegistration'
import { Reports } from './pages/Reports'
import { VendorContacts } from './pages/VendorContacts'
import { Employees } from './pages/Employees'
import { Notifications } from './pages/Notifications'
import './App.css'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/services/register" element={<ServiceRegistration />} />
        <Route path="/reports" element={<Reports />} />
        <Route path="/contacts" element={<VendorContacts />} />
        <Route path="/employees" element={<Employees />} />
        <Route path="/notifications" element={<Notifications />} />
      </Routes>
    </Router>
  )
}

export default App
