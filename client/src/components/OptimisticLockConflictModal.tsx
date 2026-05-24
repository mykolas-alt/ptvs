import '../styles/OptimisticLockConflictModal.css'

type Props = {
  entityLabel: string
  onReload: () => Promise<void> | void
  onForce: () => Promise<void> | void
  onClose: () => void
  isLoading?: boolean
}

export function OptimisticLockConflictModal({ entityLabel, onReload, onForce, onClose, isLoading }: Props) {
  return (
    <div className="olc-overlay" onClick={onClose}>
      <div className="olc-modal" onClick={e => e.stopPropagation()}>
        <div className="olc-header">
          <span className="olc-icon">⚠</span>
          <h2 className="olc-title">Update Conflict</h2>
          <button className="olc-close" onClick={onClose} disabled={isLoading}>✕</button>
        </div>
        <div className="olc-body">
          <p className="olc-message">
            This <strong>{entityLabel}</strong> was modified by another user since you opened it.
            Your changes were not saved.
          </p>
          <div className="olc-options">
            <div className="olc-option" onClick={isLoading ? undefined : onReload}>
              <div className="olc-option-title">Reload data</div>
              <div className="olc-option-desc">Discard your changes and reload the latest version.</div>
            </div>
            <div className="olc-option olc-option-force" onClick={isLoading ? undefined : onForce}>
              <div className="olc-option-title">Force update</div>
              <div className="olc-option-desc">Overwrite the other user's changes with your version.</div>
            </div>
          </div>
          {isLoading && <p className="olc-loading">Working…</p>}
        </div>
      </div>
    </div>
  )
}
