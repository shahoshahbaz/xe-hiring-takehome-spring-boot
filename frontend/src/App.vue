<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { state } from './state'

const newFromCurrency = ref('USD')
const newToCurrency = ref('CAD')
const newThreshold = ref('')
const newDirection = ref('above')

const newPair = computed(() => `${newFromCurrency.value}/${newToCurrency.value}`)

function loadRates() {
  fetch('/api/rates')
    .then((r) => r.json())
    .then((data) => {
      state.rates = data
      state.lastUpdated = new Date().toLocaleTimeString()
    })
}

function loadAlerts() {
  fetch('/api/alerts')
    .then((r) => r.json())
    .then((data) => {
      state.alerts = data
    })
}

function loadCurrencies() {
  fetch('/api/rates/pairs')
    .then((r) => r.json())
    .then((data) => {
      state.currencies = data
    })
}

function createAlert() {
  if (!newThreshold.value) return
  fetch('/api/alerts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      pair: newPair.value,
      threshold: parseFloat(newThreshold.value),
      direction: newDirection.value,
    }),
  }).then(() => {
    newThreshold.value = ''
    loadAlerts()
  })
}

function deleteAlert(id: string) {
  if (!confirm('Are you sure you want to delete this alert?')) return
  fetch(`/api/alerts/${id}`, { method: 'DELETE' }).then(() => loadAlerts())
}

function refresh() {
  loadRates()
  loadAlerts()
  loadCurrencies()
}

onMounted(() => {
  loadRates()
  loadAlerts()
  loadCurrencies()
})
</script>

<template>
  <main class="page">
    <header class="header">
      <h1>Xe Rate Board</h1>
      <span class="updated" v-if="state.lastUpdated">Last updated {{ state.lastUpdated }}</span>
    </header>

    <section class="cards">
      <div class="card" v-for="rate in state.rates" :key="rate.pair">
        <div class="pair">{{ rate.pair.replace('/', ' / ') }}</div>
        <div class="rate">{{ rate.rate.toFixed(4) }}</div>
      </div>
    </section>

    <button class="refresh" @click="refresh">Refresh</button>

    <section class="alerts-section">
      <h2>Rate Alerts</h2>

      <div class="alert-form">
        <select v-model="newFromCurrency">
          <option v-for="c in state.currencies" :key="c.iso" :value="c.iso">
            {{ c.iso }} - {{ c.currencyName }}
          </option>
        </select>

        <span class="separator">/</span>

        <select v-model="newToCurrency">
          <option v-for="c in state.currencies" :key="c.iso" :value="c.iso">
            {{ c.iso }} - {{ c.currencyName }}
          </option>
        </select>

        <select v-model="newDirection">
          <option value="above">Above</option>
          <option value="below">Below</option>
        </select>

        <input
          v-model="newThreshold"
          type="number"
          step="0.0001"
          placeholder="Threshold e.g. 1.3800"
        />

        <button class="refresh" @click="createAlert">Add Alert</button>
      </div>

      <div v-if="state.alerts.length === 0" class="no-alerts">No alerts yet.</div>

      <div
        v-for="alert in state.alerts"
        :key="alert.id"
        class="alert-row"
        :class="{ triggered: alert.triggered, error: alert.evaluationError }"
      >
        <span class="alert-info">
          {{ alert.pair }} {{ alert.direction }} {{ alert.threshold }}
        </span>
        <span class="alert-status" v-if="alert.evaluationError">
          ⚠️ {{ alert.evaluationError }}
        </span>
        <span class="alert-status" v-else>
          {{ alert.triggered ? '🔔 Triggered' : '⏳ Watching' }}
        </span>
        <button class="delete-btn" @click="deleteAlert(alert.id)">Delete</button>
      </div>
    </section>
  </main>
</template>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: 'Segoe UI', system-ui, sans-serif;
  background: #f4f6f8;
  color: #1a2233;
}

.page {
  max-width: 860px;
  margin: 0 auto;
  padding: 32px 20px;
}

.header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 24px;
}

h1 {
  font-size: 1.6rem;
  margin: 0;
}

.updated {
  font-size: 0.85rem;
  color: #66718a;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.card {
  background: #ffffff;
  border: 1px solid #e1e6ee;
  border-radius: 10px;
  padding: 20px;
}

.pair {
  font-size: 0.9rem;
  font-weight: 600;
  color: #66718a;
  letter-spacing: 0.04em;
}

.rate {
  font-size: 2rem;
  font-weight: 700;
  margin: 8px 0 4px;
  font-variant-numeric: tabular-nums;
}

.refresh {
  margin-top: 24px;
  padding: 10px 18px;
  border: none;
  border-radius: 8px;
  background: #16345c;
  color: #ffffff;
  font-size: 0.9rem;
  cursor: pointer;
}

.refresh:hover {
  background: #1d4377;
}

.alerts-section {
  margin-top: 40px;
}

.alerts-section h2 {
  font-size: 1.2rem;
  margin-bottom: 16px;
}

.alert-form {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  align-items: center;
}

.alert-form select,
.alert-form input {
  padding: 8px 12px;
  border: 1px solid #e1e6ee;
  border-radius: 8px;
  font-size: 0.9rem;
}

.separator {
  font-weight: 700;
  font-size: 1.2rem;
  color: #66718a;
}

.alert-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #ffffff;
  border: 1px solid #e1e6ee;
  border-radius: 8px;
  margin-bottom: 8px;
}

.alert-row.triggered {
  border-color: #f0a500;
  background: #fffbf0;
}

.alert-row.error {
  border-color: #cc3333;
  background: #fff0f0;
}

.alert-info {
  flex: 1;
  font-size: 0.95rem;
}

.alert-status {
  font-size: 0.85rem;
  font-weight: 600;
}

.delete-btn {
  padding: 6px 12px;
  border: 1px solid #e1e6ee;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  font-size: 0.85rem;
  color: #cc3333;
}

.delete-btn:hover {
  background: #fff0f0;
}

.no-alerts {
  color: #8a93a8;
  font-size: 0.9rem;
}
</style>