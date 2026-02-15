export default function PolicyReasonBlock({ reasons = [], summary }) {
  if (!reasons?.length && !summary) return null;

  return (
    <>
      <div style={{ marginTop: 12 }}>
        <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 8 }}>
          추천 근거
        </div>

        {summary ? (
          <div style={{ fontSize: 13, color: "#666", marginBottom: 10 }}>
            {summary}
          </div>
        ) : null}

        <ul style={{ paddingLeft: 18, margin: 0 }}>
          {reasons.map((text, idx) => (
            <li key={`${idx}-${text}`} style={{ fontSize: 13, marginBottom: 6 }}>
              {text}
            </li>
          ))}
        </ul>
      </div>
    </>
  );
}
