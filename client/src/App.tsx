import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { Home } from './pages/Home'
import { Login } from './pages/Login'
import { Dashboard } from './pages/Dashboard'
import { ServiceRegistration } from './pages/ServiceRegistration'
import { Reports } from './pages/Reports'
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
      </Routes>
    </Router>
  )
}

export default App
