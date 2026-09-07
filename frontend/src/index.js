import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';

/**
 * index.js - The entry point of the React application.
 * 
 * ReactDOM.createRoot() attaches our React app to the <div id="root"> in index.html.
 * React.StrictMode is a development tool that helps catch potential problems.
 */
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
