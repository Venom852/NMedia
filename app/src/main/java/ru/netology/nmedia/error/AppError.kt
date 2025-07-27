package ru.netology.nmedia.error

sealed class AppError(var code: String) : RuntimeException()

class ApiError(val status: Int, code: String): AppError(code)

object NetworkError : AppError("error_network") {
    private fun readResolve(): Any = NetworkError
}

object UnknownError: AppError("error_unknown") {
    private fun readResolve(): Any = UnknownError
}

object ErrorCode400And500: AppError("error_code_400_and_500") {
    private fun readResolve(): Any = ErrorCode400And500

}