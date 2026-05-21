import { useState, useEffect } from 'react'
import { useAuth, tokenStorageKey } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import '../styles/Reports.css'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api'

type ReportDetail = {
  serviceName: string
  vendorName: string
  monthlyRate: number
  calculatedRangeCost: number
  daysActiveInRange: number
}

type ReportData = {
  startDate: string
  endDate: string
  totalCost: number
  costByServiceType: Record<string, number>
  details: ReportDetail[]
}

type ReportSummary = {
  id: string
  startDate: string
  endDate: string
  totalCost: number
}

function formatReportContent(data: ReportData): string {
  const lines: string[] = [
    'THIRD-PARTY SERVICE REPORT',
    `Period: ${data.startDate} – ${data.endDate}`,
    '',
    'SUMMARY',
    '-------',
    `Total Cost: €${Number(data.totalCost).toLocaleString()}`,
  ]

  if (Object.keys(data.costByServiceType).length > 0) {
    lines.push('', 'COST BY TYPE', '------------')
    for (const [type, cost] of Object.entries(data.costByServiceType)) {
      lines.push(`${type}: €${Number(cost).toLocaleString()}`)
    }
  }

  if (data.details.length > 0) {
    lines.push('', 'SERVICES IN PERIOD', '------------------')
    data.details.forEach((d, i) => {
      lines.push(
        `${i + 1}. ${d.serviceName} (${d.vendorName})`,
        `   Monthly Rate: €${Number(d.monthlyRate).toLocaleString()}  |  Days Active: ${d.daysActiveInRange}  |  Period Cost: €${Number(d.calculatedRangeCost).toLocaleString()}`,
      )
    })
  }

  lines.push('', 'END OF REPORT')
  return lines.join('\n')
}

export function Reports() {
  const { userInfo, isLoading, logout } = useAuth()
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [savedReports, setSavedReports] = useState<ReportSummary[]>([])
  const [savedReportsLoading, setSavedReportsLoading] = useState(true)
  const [activeReportData, setActiveReportData] = useState<ReportData | null>(null)
  const [viewLoading, setViewLoading] = useState(false)
  const [isGenerating, setIsGenerating] = useState(false)
  const [dateError, setDateError] = useState<string | null>(null)
  const [generateError, setGenerateError] = useState<string | null>(null)

  function getToken() { return localStorage.getItem(tokenStorageKey) }

  async function loadReports() {
    try {
      const res = await fetch(`${apiBaseUrl}/reports/cost-report?size=100`, {
        headers: { Authorization: `Bearer ${getToken()}` },
      })
      if (!res.ok) return
      const page = await res.json() as { content: ReportSummary[] }
      setSavedReports(Array.isArray(page.content) ? page.content : [])
    } catch {
      // ignore — list just stays empty
    }
  }

  useEffect(() => {
    if (isLoading) return
    loadReports().finally(() => setSavedReportsLoading(false))
  }, [isLoading])

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  async function handleGenerate() {
    setDateError(null)
    setGenerateError(null)

    if (!startDate || !endDate) {
      setDateError('Please select both a start date and an end date.')
      return
    }
    if (endDate <= startDate) {
      setDateError('End date must be after start date.')
      return
    }

    setIsGenerating(true)
    try {
      const res = await fetch(`${apiBaseUrl}/reports/cost-report`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${getToken()}`,
        },
        body: JSON.stringify({ startDate, endDate }),
      })
      if (!res.ok) throw new Error('Failed to generate report.')
      const data = await res.json() as ReportData
      await loadReports()
      setActiveReportData(data)
    } catch (err) {
      setGenerateError(err instanceof Error ? err.message : 'Report generation failed.')
    } finally {
      setIsGenerating(false)
    }
  }

  async function handleView(id: string) {
    setViewLoading(true)
    setActiveReportData(null)
    try {
      const res = await fetch(`${apiBaseUrl}/reports/cost-report/${id}`, {
        headers: { Authorization: `Bearer ${getToken()}` },
      })
      if (!res.ok) throw new Error()
      const data = await res.json() as ReportData
      setActiveReportData(data)
    } catch {
      setGenerateError('Failed to load report.')
    } finally {
      setViewLoading(false)
    }
  }

  return (
    <div className="page-container">
      <Navbar userInfo={userInfo} onLogout={logout} />

      <main className="reports-main">
        <h1>Reports</h1>

        <div className="report-card">
          <h2>Generate Report</h2>
          <div className="report-date-row">
            <div className="form-field">
              <label htmlFor="report-start">Start Date</label>
              <input
                id="report-start"
                type="date"
                value={startDate}
                onChange={e => { setStartDate(e.target.value); setDateError(null); setGenerateError(null) }}
              />
            </div>
            <div className="form-field">
              <label htmlFor="report-end">End Date</label>
              <input
                id="report-end"
                type="date"
                value={endDate}
                onChange={e => { setEndDate(e.target.value); setDateError(null); setGenerateError(null) }}
              />
            </div>
            <button className="btn-primary" onClick={handleGenerate} disabled={isGenerating || viewLoading}>
              {isGenerating ? 'Generating…' : 'Generate'}
            </button>
          </div>
          {dateError && <p className="field-error">{dateError}</p>}
          {generateError && <p className="field-error">{generateError}</p>}
        </div>

        <div className="report-log">
          <h2>Previously Generated Reports</h2>
          {savedReportsLoading ? (
            <p className="empty-log">Loading...</p>
          ) : savedReports.length === 0 ? (
            <p className="empty-log">No reports generated yet.</p>
          ) : (
            <table className="reports-table">
              <thead>
                <tr>
                  <th>Period Start</th>
                  <th>Period End</th>
                  <th>Total Cost</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {savedReports.map(r => (
                  <tr key={r.id}>
                    <td>{r.startDate}</td>
                    <td>{r.endDate}</td>
                    <td>€{Number(r.totalCost).toLocaleString()}</td>
                    <td>
                      <button className="link-btn" onClick={() => handleView(r.id)} disabled={viewLoading}>
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </main>

      {(activeReportData || viewLoading) && (
        <div className="modal-overlay" onClick={() => { setActiveReportData(null); setViewLoading(false) }}>
          <div className="modal report-modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>
                {activeReportData ? `Report: ${activeReportData.startDate} – ${activeReportData.endDate}` : 'Loading report…'}
              </h2>
              <button className="modal-close" onClick={() => { setActiveReportData(null); setViewLoading(false) }}>✕</button>
            </div>
            <div className="modal-body">
              {activeReportData
                ? <pre className="report-content">{formatReportContent(activeReportData)}</pre>
                : <p className="loading-text">Loading…</p>
              }
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
