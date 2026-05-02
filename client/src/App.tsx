import './App.css'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'

function App() {
  return (
    <main style={{ maxWidth: 700, margin: '4rem auto', fontFamily: 'Inter, sans-serif' }}>
      <h1>PTVS Client</h1>
      <p>This frontend is deployed separately and consumes the Spring API.</p>
      <p>
        Current API base URL: <code>{apiBaseUrl}</code>
      </p>
    </main>
  )
}

export default App
