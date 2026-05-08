import { useState } from 'react'
import { useAuth } from '../hooks/useAuth'
import { Navbar } from '../components/Navbar'
import '../styles/Reports.css'

type Report = {
  id: string
  startDate: string
  endDate: string
  generatedAt: string
  content: string
}

function buildReportContent(start: string, end: string): string {
  return `THIRD-PARTY SERVICE REPORT
Period: ${start} – ${end}
Generated: ${new Date().toLocaleString()}

SUMMARY
-------
Active Services:   2
Ended Services:    2
Pending Services:  1
Total Monthly Cost: $1,900

SERVICES IN PERIOD
------------------
1. AWS Cloud (Amazon Web Services)
   Status: Active  |  Monthly Cost: $1,200
   Contract: 2023-01-15 – 2025-01-14

2. GitHub Enterprise (GitHub Inc.)
   Status: Ended  |  Monthly Cost: $250
   Contract: 2022-06-01 – 2024-05-31

3. Slack Business+ (Salesforce / Slack)
   Status: Pending  |  Monthly Cost: $180
   Contract: 2026-03-01 – 2027-02-28

4. Jira Cloud (Atlassian)
   Status: Active  |  Monthly Cost: $320
   Contract: 2024-04-01 – 2026-03-31

5. Zoom Pro (Zoom Video Communications)
   Status: Ended  |  Monthly Cost: $150
   Contract: 2021-09-01 – 2023-08-31

END OF REPORT`
}

export function Reports() {
  const { userInfo, isLoading, logout } = useAuth()
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [reports, setReports] = useState<Report[]>([])
  const [activeReport, setActiveReport] = useState<Report | null>(null)
  const [isGenerating, setIsGenerating] = useState(false)
  const [dateError, setDateError] = useState<string | null>(null)
  const [duplicateId, setDuplicateId] = useState<string | null>(null)

  if (isLoading) {
    return <div className="page-container"><p className="loading-text">Loading...</p></div>
  }

  function handleGenerate() {
    setDateError(null)
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
    setTimeout(() => {
      const report: Report = {
        id: crypto.randomUUID(),
        startDate,
        endDate,
        generatedAt: new Date().toLocaleString(),
        content: buildReportContent(startDate, endDate),
      }
      setReports(prev => [report, ...prev])
      setActiveReport(report)
      setIsGenerating(false)
    }, 800)
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
                onChange={e => { setStartDate(e.target.value); setDateError(null); setDuplicateId(null) }}
              />
            </div>
            <div className="form-field">
              <label htmlFor="report-end">End Date</label>
              <input
                id="report-end"
                type="date"
                value={endDate}
                onChange={e => { setEndDate(e.target.value); setDateError(null); setDuplicateId(null) }}
              />
            </div>
            <button className="btn-primary" onClick={handleGenerate} disabled={isGenerating}>
              {isGenerating ? 'Generating…' : 'Generate'}
            </button>
          </div>
          {dateError && <p className="field-error">{dateError}</p>}
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
              <pre className="report-content">{activeReport.content}</pre>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
