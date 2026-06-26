import { reactive } from 'vue'

export interface Rate {
  pair: string
  rate: number
}

export interface Alert {
  id: string
  pair: string
  threshold: number
  direction: string
  triggered: boolean
  createdAt: string
}

export const state = reactive({
  rates: [] as Rate[],
  alerts: [] as Alert[],
  lastUpdated: '',
})