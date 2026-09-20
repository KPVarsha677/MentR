import React from 'react';
import { Link } from 'react-router-dom';
import styles from './LandingPage.module.css';

/**
 * LandingPage - public marketing hero shown at "/" before a visitor signs in.
 */
function LandingPage() {
  return (
    <div className={styles.landing}>
      <nav className={styles.nav} style={{ position: 'relative', zIndex: 2, paddingInline: 'clamp(20px,5vw,72px)', animation: 'om-fade .5s ease-out both' }}>
        <span className={styles.navBrand}>MentR</span>
        <a href="#portfolio">Portfolio</a>
        <a href="#classrooms">Classrooms</a>
        <a href="#reports">Reports</a>
        <Link to="/login" style={{ marginLeft: 'auto', fontSize: 14 }}>Sign in</Link>
        <Link to="/register" className={`${styles.btn} ${styles.btnPrimary} ${styles.blueprint}`}>
          Get started
          <i className={`${styles.corner} ${styles.tl}`}></i>
          <i className={`${styles.corner} ${styles.tr}`}></i>
          <i className={`${styles.corner} ${styles.bl}`}></i>
          <i className={`${styles.corner} ${styles.br}`}></i>
        </Link>
      </nav>

      <section style={{ position: 'relative', overflow: 'hidden', padding: 'clamp(40px,6vw,88px) clamp(20px,5vw,72px) clamp(56px,7vw,104px)' }}>
        <div
          aria-hidden="true"
          style={{
            position: 'absolute', inset: 0, pointerEvents: 'none',
            backgroundImage:
              'repeating-linear-gradient(to right, color-mix(in srgb, var(--color-text) 6%, transparent) 0 1px, transparent 1px 96px),' +
              'repeating-linear-gradient(to bottom, color-mix(in srgb, var(--color-text) 6%, transparent) 0 1px, transparent 1px 96px)',
            backgroundPosition: 'center top',
            maskImage: 'radial-gradient(120% 90% at 50% 40%, #000 25%, transparent 78%)',
            animation: 'om-fade 1.2s ease-out both',
          }}
        ></div>

        <div style={{ position: 'relative', zIndex: 1, maxWidth: 1280, margin: '0 auto', display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'center', gap: 'clamp(28px,4vw,64px)' }}>

          {/* Left column */}
          <div style={{ flex: '1 1 240px', maxWidth: 330, minWidth: 0, display: 'flex', flexDirection: 'column', gap: 'clamp(28px,4vw,52px)' }}>
            <div className={styles.blueprint} style={{ padding: 18, marginTop: 24, animation: 'om-rise .7s ease-out .25s both' }}>
              <i className={`${styles.corner} ${styles.tl}`}></i>
              <i className={`${styles.corner} ${styles.tr}`}></i>
              <i className={`${styles.corner} ${styles.bl}`}></i>
              <i className={`${styles.corner} ${styles.br}`}></i>
              <span style={{ display: 'block', fontSize: 12, fontWeight: 600, letterSpacing: '0.08em', textTransform: 'uppercase', color: 'var(--color-accent-700)' }}>Portfolio</span>
              <div style={{ height: 1, background: 'var(--color-divider)', margin: '12px 0 14px' }}></div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 11 }}>
                {[['Projects', '06', 72, '.8s'], ['Skills', '12', 90, '.95s'], ['Certificates', '04', 48, '1.1s']].map(([label, value, pct, delay]) => (
                  <React.Fragment key={label}>
                    <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', gap: 12 }}>
                      <span style={{ fontSize: 14, color: 'color-mix(in srgb, var(--color-text) 78%, transparent)' }}>{label}</span>
                      <span style={{ fontFamily: 'var(--font-heading)', fontWeight: 600, fontSize: 22, fontFeatureSettings: "'tnum' 1" }}>{value}</span>
                    </div>
                    <div style={{ height: 4, background: 'var(--color-accent-200)' }}>
                      <div style={{ height: 4, width: `${pct}%`, background: 'var(--color-accent-600)', transformOrigin: 'left', animation: `om-bar 1s cubic-bezier(.2,.7,.2,1) ${delay} both` }}></div>
                    </div>
                  </React.Fragment>
                ))}
              </div>
            </div>

            <div className={styles.blueprint} style={{ padding: '16px 18px', marginLeft: 'clamp(0px,3vw,36px)', animation: 'om-rise .7s ease-out .45s both' }}>
              <i className={`${styles.corner} ${styles.tl}`}></i>
              <i className={`${styles.corner} ${styles.tr}`}></i>
              <i className={`${styles.corner} ${styles.bl}`}></i>
              <i className={`${styles.corner} ${styles.br}`}></i>
              <span style={{ display: 'block', fontSize: 12, fontWeight: 600, letterSpacing: '0.08em', textTransform: 'uppercase', color: 'var(--color-accent-700)' }}>Join code</span>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(8,minmax(0,1fr))', gap: 4, marginTop: 12 }}>
                {['7', 'Q', 'K', '2', 'W', 'D', '4', 'M'].map((ch, i) => (
                  <span key={i} style={{ border: '1px solid var(--color-divider)', textAlign: 'center', padding: '6px 0', fontFamily: 'var(--font-heading)', fontWeight: 600, fontSize: 18 }}>{ch}</span>
                ))}
              </div>
            </div>
          </div>

          {/* Center column */}
          <div style={{ flex: '2 1 470px', maxWidth: 660, minWidth: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
            <span className={styles.blueprint} style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', width: 44, height: 44, fontFamily: 'var(--font-heading)', fontWeight: 600, fontSize: 24, color: 'var(--color-accent-700)', animation: 'om-rise .6s ease-out both' }}>
              M
              <i className={`${styles.corner} ${styles.tl}`}></i>
              <i className={`${styles.corner} ${styles.tr}`}></i>
              <i className={`${styles.corner} ${styles.bl}`}></i>
              <i className={`${styles.corner} ${styles.br}`}></i>
            </span>
            <h1 style={{ fontFamily: 'var(--font-heading)', fontWeight: 600, fontSize: 'clamp(42px,6.4vw,88px)', lineHeight: 1.04, letterSpacing: '0.01em', textTransform: 'uppercase', margin: 'clamp(24px,3vw,40px) 0 0' }}>
              <span style={{ display: 'block', animation: 'om-rise .7s ease-out .08s both' }}>Your digital</span>
              <span style={{ display: 'block', color: 'var(--color-accent-700)', animation: 'om-rise .7s ease-out .18s both' }}>mentor journal</span>
            </h1>
            <p style={{ fontSize: 17, lineHeight: 1.5, maxWidth: '34ch', margin: 'clamp(16px,2vw,24px) 0 0', color: 'color-mix(in srgb, var(--color-text) 82%, transparent)', animation: 'om-rise .7s ease-out .28s both' }}>
              Projects, certificates and achievements &mdash; verified by your mentor.
            </p>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, justifyContent: 'center', marginTop: 'clamp(20px,2.5vw,28px)', animation: 'om-rise .7s ease-out .38s both' }}>
              <Link to="/register" className={`${styles.btn} ${styles.btnPrimary} ${styles.blueprint}`}>
                Get started
                <i className={`${styles.corner} ${styles.tl}`}></i>
                <i className={`${styles.corner} ${styles.tr}`}></i>
                <i className={`${styles.corner} ${styles.bl}`}></i>
                <i className={`${styles.corner} ${styles.br}`}></i>
              </Link>
              <a href="#portfolio" className={`${styles.btn} ${styles.btnGhost}`}>How it works</a>
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, justifyContent: 'center', marginTop: 'clamp(24px,3vw,36px)', animation: 'om-fade .8s ease-out .55s both' }}>
              {['Portfolios', 'Verification', 'Classrooms', 'Reports'].map((label) => (
                <span key={label} className={`${styles.tag} ${styles.tagOutline}`}>{label}</span>
              ))}
            </div>
          </div>

          {/* Right column */}
          <div style={{ flex: '1 1 240px', maxWidth: 330, minWidth: 0, display: 'flex', flexDirection: 'column', gap: 'clamp(28px,4vw,52px)' }}>
            <div className={styles.blueprint} style={{ padding: '16px 18px', marginRight: 'clamp(0px,2vw,24px)', animation: 'om-rise .7s ease-out .35s both' }}>
              <i className={`${styles.corner} ${styles.tl}`}></i>
              <i className={`${styles.corner} ${styles.tr}`}></i>
              <i className={`${styles.corner} ${styles.bl}`}></i>
              <i className={`${styles.corner} ${styles.br}`}></i>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--color-accent-700)" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" style={{ animation: 'om-drift 2.4s ease-in-out 1.4s 2 alternate both' }}>
                  <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"></path>
                  <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"></path>
                </svg>
                <span style={{ fontSize: 12, fontWeight: 600, letterSpacing: '0.08em', textTransform: 'uppercase', color: 'var(--color-accent-700)' }}>Inbox</span>
                <span aria-hidden="true" style={{ width: 6, height: 6, background: 'var(--color-accent-600)', marginLeft: 'auto', animation: 'om-blink 2s ease-in-out 1.2s infinite' }}></span>
              </div>
              <div style={{ height: 1, background: 'var(--color-divider)', margin: '12px 0 14px' }}></div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 10 }}>
                  <span style={{ fontSize: 14, lineHeight: 1.35 }}>Alice &middot; project</span>
                  <span className={`${styles.tag} ${styles.tagAccent}`} style={{ flex: 'none' }}>Pending</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 10 }}>
                  <span style={{ fontSize: 14, lineHeight: 1.35 }}>Ravi &middot; certificate</span>
                  <span className={`${styles.tag} ${styles.tagOutline}`} style={{ flex: 'none' }}>Verified</span>
                </div>
              </div>
            </div>

            <div className={styles.blueprint} style={{ padding: 18, animation: 'om-rise .8s ease-out .55s both' }}>
              <i className={`${styles.corner} ${styles.tl}`}></i>
              <i className={`${styles.corner} ${styles.tr}`}></i>
              <i className={`${styles.corner} ${styles.bl}`}></i>
              <i className={`${styles.corner} ${styles.br}`}></i>
              <span style={{ display: 'block', fontSize: 12, fontWeight: 600, letterSpacing: '0.08em', textTransform: 'uppercase', color: 'var(--color-accent-700)' }}>Verification flow</span>
              <div style={{ height: 1, background: 'var(--color-divider)', margin: '12px 0 16px' }}></div>
              <div style={{ display: 'grid', gridTemplateColumns: '14px 1fr', columnGap: 14, rowGap: 0 }}>
                <div style={{ gridColumn: 1, gridRow: '1 / span 5', position: 'relative', justifySelf: 'center', width: 1, background: 'var(--color-divider)' }}>
                  <span aria-hidden="true" style={{ position: 'absolute', left: -3, width: 7, height: 7, background: 'var(--color-accent-600)', animation: 'om-travel 3.2s ease-in-out 1.2s infinite' }}></span>
                </div>
                {['Student adds an entry', 'Mentor is notified', 'Reviewed with a comment'].map((text) => (
                  <div key={text} style={{ gridColumn: 2, display: 'flex', alignItems: 'center', gap: 10, height: 34 }}>
                    <span style={{ width: 9, height: 9, border: '1px solid var(--color-accent-600)', flex: 'none' }}></span>
                    <span style={{ fontSize: 14 }}>{text}</span>
                  </div>
                ))}
                <div style={{ gridColumn: 2, display: 'flex', alignItems: 'center', gap: 10, height: 34 }}>
                  <span style={{ display: 'inline-flex', alignItems: 'center', justifyContent: 'center', width: 9, height: 9, background: 'var(--color-accent-600)', flex: 'none' }}></span>
                  <span style={{ fontSize: 14, fontWeight: 500 }}>Verified on the portfolio</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

export default LandingPage;
