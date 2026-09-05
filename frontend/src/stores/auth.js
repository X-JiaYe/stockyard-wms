import { defineStore } from 'pinia'

const TOKEN_KEY = 'wms_token'
const USERNAME_KEY = 'wms_username'
const NICKNAME_KEY = 'wms_nickname'
const ROLES_KEY = 'wms_roles'

function readRoles() {
  try {
    return JSON.parse(localStorage.getItem(ROLES_KEY) || '[]')
  } catch {
    return []
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    username: localStorage.getItem(USERNAME_KEY) || '',
    nickname: localStorage.getItem(NICKNAME_KEY) || '',
    roles: readRoles()
  }),
  getters: {
    isLoggedIn: (s) => !!s.token,
    isAdmin: (s) => s.roles.includes('ADMIN'),
    displayName: (s) => s.nickname || s.username
  },
  actions: {
    setLogin({ token, username, nickname, roles }) {
      this.token = token
      this.username = username
      this.nickname = nickname || ''
      this.roles = roles || []
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USERNAME_KEY, username)
      localStorage.setItem(NICKNAME_KEY, this.nickname)
      localStorage.setItem(ROLES_KEY, JSON.stringify(this.roles))
    },
    logout() {
      this.token = ''
      this.username = ''
      this.nickname = ''
      this.roles = []
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USERNAME_KEY)
      localStorage.removeItem(NICKNAME_KEY)
      localStorage.removeItem(ROLES_KEY)
    }
  }
})
