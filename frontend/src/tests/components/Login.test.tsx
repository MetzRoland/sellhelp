import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, it, vi, beforeEach, expect } from 'vitest'
import { MemoryRouter } from 'react-router'

import Login from '../../components/Login/Login'
import type { AuthContextType } from '../../contextProviders/AuthProvider/AuthProviderTypes'

const mockNavigate = vi.fn()

vi.mock('react-router', async () => {
  const actual = await vi.importActual<any>('react-router')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

const mockLoginLocal = vi.fn()
const mockVerifyTotp = vi.fn()
const mockHandleGoogleLogin = vi.fn()
const mockSetValidationErrors = vi.fn()
const mockSetAuthError = vi.fn()

let authMockState: AuthContextType;

vi.mock('../../contextProviders/AuthProvider/AuthContext', () => ({
  useAuth: () => authMockState,
}))

vi.mock(
  '../../contextProviders/ProccessLoadProvider/ProccessLoadContext',
  () => ({
    useLoading: () => ({
      setIsLoading: vi.fn(),
      setLoadingMessage: vi.fn(),
    }),
  }),
)

vi.mock('../components/Header/Header', () => ({
  default: () => <div data-testid="header" />,
}))

vi.mock('../components/Footer/Footer', () => ({
  default: () => <div data-testid="footer" />,
}))

const renderLogin = (isAdminLogin = false) => {
  render(
    <MemoryRouter>
      <Login isAdminLogin={isAdminLogin} />
    </MemoryRouter>,
  )
}

beforeEach(() => {
  vi.clearAllMocks()

  authMockState = {
    loginLocal: mockLoginLocal,
    tempToken: null,
    verifyTotp: mockVerifyTotp,
    validationErrors: {
      email: '',
      password: '',
      totpCode: '',
    },
    setValidationErrors: mockSetValidationErrors,
    authError: '',
    setAuthError: mockSetAuthError,
    user: null,
    handleGoogleLogin: mockHandleGoogleLogin,
  }
})

describe('Login component', () => {
  it('renders login form', () => {
    renderLogin()

    expect(screen.getByRole('button', { name: 'Bejelentkezés' })).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Email')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Jelszó')).toBeInTheDocument()
  })

  it('submits login form and calls loginLocal', async () => {
    const user = userEvent.setup()
    renderLogin()

    await user.type(
      screen.getByPlaceholderText('Email'),
      'test@test.com',
    )
    await user.type(
      screen.getByPlaceholderText('Jelszó'),
      'password123',
    )

    await user.click(
      screen.getByRole('button', { name: 'Bejelentkezés' }),
    )

    expect(mockLoginLocal).toHaveBeenCalledWith('/auth/login', {
      email: 'test@test.com',
      password: 'password123',
    })
  })

  it('calls google login when clicking Google button', async () => {
    const user = userEvent.setup()
    renderLogin()

    await user.click(
      screen.getByRole('button', { name: /google/i }),
    )

    expect(mockHandleGoogleLogin).toHaveBeenCalled()
  })

  it('renders TOTP form when tempToken exists', () => {
    authMockState.tempToken = 'temp-token'

    renderLogin()

    expect(
      screen.getByPlaceholderText('Hitelesítő kód'),
    ).toBeInTheDocument()
  })

  it('calls verifyTotp when submitting TOTP form', async () => {
    authMockState.tempToken = 'temp-token'

    const user = userEvent.setup()
    renderLogin()

    await user.type(
      screen.getByPlaceholderText('Hitelesítő kód'),
      '123456',
    )

    await user.click(
      screen.getByRole('button', { name: 'Ellenőrzés' }),
    )

    expect(mockVerifyTotp).toHaveBeenCalledWith({
      totpCode: '123456',
      tempToken: 'temp-token',
    })
  })

  it('navigates to inactive profile if user is blocked', () => {
    authMockState.authError = 'A felhasználó le van tiltva!'

    renderLogin()

    expect(mockNavigate).toHaveBeenCalledWith('/profileInactive')
  })

  it('renders admin login title when isAdminLogin is true', () => {
    renderLogin(true)

    expect(screen.getByText('Admin Belépés')).toBeInTheDocument()
  })
})
