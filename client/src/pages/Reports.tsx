import { useState } from 'react'
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

type Report = {
  id: string
  startDate: string
  endDate: string
  generatedAt: string
  data: ReportData
}

function formatReportContent(data: ReportData): string {
  const lines: string[] = [
    'THIRD-PARTY SERVICE REPORT',
    `Period: ${data.startDate} – ${data.endDate}`,
    '',
    'SUMMARY',
    '-------',
    `Total Cost: $${Number(data.totalCost).toLocaleString()}`,
  ]

  if (Object.keys(data.costByServiceType).length > 0) {
    lines.push('', 'COST BY TYPE', '------------')
    for (const [type, cost] of Object.entries(data.costByServiceType)) {
      lines.push(`${type}: $${Number(cost).toLocaleString()}`)
    }
  }

  if (data.details.length > 0) {
    lines.push('', 'SERVICES IN PERIOD', '------------------')
    data.details.forEach((d, i) => {
      lines.push(
        `${i + 1}. ${d.serviceName} (${d.vendorName})`,
        `   Monthly Rate: $${Number(d.monthlyRate).toLocaleString()}  |  Days Active: ${d.daysActiveInRange}  |  Period Cost: $${Number(d.calculatedRangeCost).toLocaleString()}`,
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
  const [reports, setReports] = useState<Report[]>([])
  const [activeReport, setActiveReport] = useState<Report | null>(null)
  const [isGenerating, setIsGenerating] = useState(false)
  const [dateError, setDateError] = useState<string | null>(null)
  const [generateError, setGenerateError] = useState<string | null>(null)
  const [duplicateId, setDuplicateId] = useState<string | null>(null)

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  async function handleGenerate() {
    setDateError(null)
    setGenerateError(null)
    setDuplicateId(null)

    if (!startDate || !endDate) {
      setDateError('Please select both a start date and an end date.')
      return
    }
    if (endDate <= startDate) {
      setDateError('End date must be after start date.')
      return
    }

    const existing = reports.find(r => r.startDate === startDate && r.endDate === endDate)
    if (existing) {
      setDuplicateId(existing.id)
      setActiveReport(existing)
      return
    }

    setIsGenerating(true)

    try {
      const token = localStorage.getItem(tokenStorageKey)
      const res = await fetch(`${apiBaseUrl}/reports/cost-report`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ startDate, endDate }),
      })
      if (!res.ok) throw new Error('Failed to generate report.')
      const data = await res.json() as ReportData

      const report: Report = {
        id: crypto.randomUUID(),
        startDate,
        endDate,
        generatedAt: new Date().toLocaleString(),
        data,
      }
      setReports(prev => [report, ...prev])
      setActiveReport(report)
    } catch (err) {
      setGenerateError(err instanceof Error ? err.message : 'Report generation failed.')
    } finally {
      setIsGenerating(false)
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
                onChange={e => { setStartDate(e.target.value); setDateError(null); setGenerateError(null); setDuplicateId(null) }}
              />
            </div>
            <div className="form-field">
              <label htmlFor="report-end">End Date</label>
              <input
                id="report-end"
                type="date"
                value={endDate}
                onChange={e => { setEndDate(e.target.value); setDateError(null); setGenerateError(null); setDuplicateId(null) }}
              />
            </div>
            <button className="btn-primary" onClick={handleGenerate} disabled={isGenerating}>
              {isGenerating ? 'Generating…' : 'Generate'}
            </button>
          </div>
          {dateError && <p className="field-error">{dateError}</p>}
          {generateError && <p className="field-error">{generateError}</p>}
          {duplicateId && (
            <p className="duplicate-notice">
              A report for this period was already generated.{' '}
              <button
                className="link-btn"
                onClick={() => setActiveReport(reports.find(r => r.id === duplicateId) ?? null)}
              >
                View it
              </button>
            </p>
          )}
        </div>

        <div className="report-log">
          <h2>Previously Generated Reports</h2>
          {reports.length === 0 ? (
            <p className="empty-log">No reports generated yet.</p>
          ) : (
            <table className="reports-table">
              <thead>
                <tr>
                  <th>Period Start</th>
                  <th>Period End</th>
                  <th>Generated At</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {reports.map(r => (
                  <tr key={r.id}>
                    <td>{r.startDate}</td>
                    <td>{r.endDate}</td>
                    <td>{r.generatedAt}</td>
                    <td>
                      <button className="link-btn" onClick={() => setActiveReport(r)}>
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

      {activeReport && (
        <div className="modal-overlay" onClick={() => setActiveReport(null)}>
          <div className="modal report-modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>
                Report: {activeReport.startDate} – {activeReport.endDate}
              </h2>
              <button className="modal-close" onClick={() => setActiveReport(null)}>✕</button>
            </div>
            <div className="modal-body">
              <pre className="report-content">{formatReportContent(activeReport.data)}</pre>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
