import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import id.co.bcaf.adapinjam.data.model.LoginResponse
import id.co.bcaf.adapinjam.data.network.ApiService
import id.co.bcaf.adapinjam.data.provider.TokenProvider
import id.co.bcaf.adapinjam.data.viewModel.LoginViewModel
import id.co.bcaf.adapinjam.utils.getOrAwaitValue
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

@ExperimentalCoroutinesApi
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: LoginViewModel
    private val apiService = mock<ApiService>()
    private val mockTokenProvider = mock<TokenProvider>()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = LoginViewModel(apiService, mockTokenProvider)
    }

    @Test
    fun `login success returns token`() = runTest {
        val dummyToken = "mock_token"
        val dummyResponse = Response.success(LoginResponse(token = dummyToken))

        whenever(apiService.login(any())).thenReturn(dummyResponse)
        whenever(mockTokenProvider.getToken(any())).thenAnswer {
            val callback = it.arguments[0] as (String?) -> Unit
            callback(dummyToken)
        }

        viewModel.login("email", "password")
        advanceUntilIdle()

        val result = viewModel.loginResult.getOrAwaitValue()
        assertTrue(result.isSuccess)
        assertEquals(dummyToken, result.getOrNull())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
