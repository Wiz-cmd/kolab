package com.kolab.data.api

import com.kolab.data.api.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface KolabApi {

    // ── Auth ────────────────────────────────────────────────────────────
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<TokenPair>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/login/verify")
    suspend fun verifyLoginOtp(@Body request: VerifyLoginOtpRequest): Response<TokenPair>

    @POST("auth/login/resend")
    suspend fun resendLoginOtp(@Body request: ResendLoginOtpRequest): Response<ResendLoginOtpResponse>

    @POST("auth/login/otp-settings")
    suspend fun setLoginOtp(@Body request: LoginOtpSettingsRequest): Response<Map<String, Any>>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<TokenPair>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshRequest): Response<Unit>

    @POST("auth/logout-all")
    suspend fun logoutAll(): Response<Unit>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    @POST("auth/request-password-reset")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequest): Response<Unit>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body body: Map<String, String>): Response<Unit>

    @POST("auth/2fa/setup")
    suspend fun setup2FA(): Response<TwoFactorSetupResponse>

    @POST("auth/2fa/enable")
    suspend fun enable2FA(@Body request: TwoFactorEnableRequest): Response<Unit>

    @POST("auth/2fa/disable")
    suspend fun disable2FA(@Body request: TwoFactorEnableRequest): Response<Unit>

    // ── Users ───────────────────────────────────────────────────────────
    @GET("users")
    suspend fun listUsers(
        @Query("role") role: String? = null,
        @Query("location") location: String? = null,
        @Query("search") search: String? = null,
        @Query("take") take: Int = 20,
        @Query("skip") skip: Int = 0
    ): Response<UserListResponse>

    @GET("users/me")
    suspend fun getMe(): Response<UserResponse>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserResponse>

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponse>

    @DELETE("users/me")
    suspend fun deleteAccount(): Response<Unit>

    @POST("users/me/portfolio")
    suspend fun addPortfolioItem(@Body request: PortfolioItemRequest): Response<PortfolioItemResponse>

    @POST("users/{id}/follow")
    suspend fun followUser(@Path("id") id: String): Response<FollowResponse>

    @DELETE("users/{id}/follow")
    suspend fun unfollowUser(@Path("id") id: String): Response<FollowResponse>

    @GET("users/{id}/followers")
    suspend fun getFollowers(
        @Path("id") id: String,
        @Query("take") take: Int = 20,
        @Query("skip") skip: Int = 0
    ): Response<UserListResponse>

    @GET("users/{id}/following")
    suspend fun getFollowing(
        @Path("id") id: String,
        @Query("take") take: Int = 20,
        @Query("skip") skip: Int = 0
    ): Response<UserListResponse>

    // ── Posts ───────────────────────────────────────────────────────────
    @GET("challenges/current")
    suspend fun currentChallenge(): Response<ChallengeResponse>

    @GET("challenges/current/leaderboard")
    suspend fun challengeLeaderboard(@Query("limit") limit: Int = 10): Response<PostListResponse>

    @GET("users/rising")
    suspend fun risingCreators(
        @Query("city") city: String? = null,
        @Query("take") take: Int = 12
    ): Response<List<UserResponse>>

    @GET("analytics/viewers")
    suspend fun profileViewers(): Response<ProfileViewersResponse>

    @GET("posts/feed")
    suspend fun getFeed(
        @Query("limit") take: Int = 20,
        @Query("skip") skip: Int = 0,
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("tags") tags: String? = null,
        @Query("location") location: String? = null
    ): Response<PostListResponse>

    @GET("posts/feed/for-you")
    suspend fun getForYouFeed(
        @Query("limit") take: Int = 20,
        @Query("skip") skip: Int = 0
    ): Response<PostListResponse>

    @GET("posts/feed/for-you/public")
    suspend fun getPublicFeed(
        @Query("limit") take: Int = 20,
        @Query("skip") skip: Int = 0
    ): Response<PostListResponse>

    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<PostResponse>

    @PATCH("posts/{id}")
    suspend fun updatePost(
        @Path("id") id: String,
        @Body request: UpdatePostRequest
    ): Response<PostResponse>

    @POST("posts/{id}/like")
    suspend fun toggleLike(@Path("id") id: String): Response<LikeResponse>

    @POST("posts/{id}/view")
    suspend fun recordView(@Path("id") id: String): Response<Unit>

    @POST("posts/{id}/collab-tap")
    suspend fun collabTap(@Path("id") id: String): Response<Unit>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: String): Response<Unit>

    @GET("posts/{id}/comments")
    suspend fun getComments(
        @Path("id") id: String,
        @Query("limit") take: Int = 50,
        @Query("skip") skip: Int = 0
    ): Response<CommentListResponse>

    @POST("posts/{id}/comments")
    suspend fun createComment(
        @Path("id") id: String,
        @Body request: CreateCommentRequest
    ): Response<CommentResponse>

    @DELETE("posts/{id}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("id") id: String,
        @Path("commentId") commentId: String
    ): Response<Unit>

    @GET("posts/interests/me")
    suspend fun getMyInterests(): Response<InterestScores>

    // ── Deals ───────────────────────────────────────────────────────────
    @POST("deals")
    suspend fun createDeal(@Body request: CreateDealRequest): Response<DealResponse>

    @GET("deals")
    suspend fun listDeals(
        @Query("take") take: Int = 20,
        @Query("skip") skip: Int = 0
    ): Response<DealListResponse>

    @GET("deals/{id}")
    suspend fun getDeal(@Path("id") id: String): Response<DealResponse>

    @PATCH("deals/{id}/status")
    suspend fun updateDealStatus(
        @Path("id") id: String,
        @Body request: DealStatusRequest
    ): Response<DealResponse>

    @POST("deals/{id}/dispute")
    suspend fun raiseDispute(
        @Path("id") id: String,
        @Body request: DisputeRequest
    ): Response<Unit>

    @POST("deals/{id}/release")
    suspend fun releaseEscrow(@Path("id") id: String): Response<Unit>

    // ── Messages ────────────────────────────────────────────────────────
    @GET("deals/{dealId}/messages")
    suspend fun getMessages(
        @Path("dealId") dealId: String,
        @Query("take") take: Int = 50,
        @Query("before") before: String? = null
    ): Response<MessageListResponse>

    @GET("deals/{dealId}/messages/read")
    suspend fun markMessagesRead(@Path("dealId") dealId: String): Response<Unit>

    // ── Hotspots ────────────────────────────────────────────────────────
    @GET("hotspots")
    suspend fun listHotspots(
        @Query("province") province: String? = null,
        @Query("category") category: String? = null,
        @Query("city") city: String? = null,
        @Query("take") take: Int = 20,
        @Query("skip") skip: Int = 0
    ): Response<HotspotListResponse>

    @GET("hotspots/leaderboard")
    suspend fun getLeaderboard(): Response<List<LeaderboardEntry>>

    @POST("hotspots")
    suspend fun createHotspot(@Body request: CreateHotspotRequest): Response<HotspotResponse>

    @POST("hotspots/{id}/checkin")
    suspend fun checkIn(@Path("id") id: String): Response<CheckInResponse>

    // ── Withdrawals ─────────────────────────────────────────────────────
    @POST("withdrawals")
    suspend fun requestWithdrawal(@Body request: WithdrawalRequest): Response<WithdrawalResponse>

    @GET("withdrawals/me")
    suspend fun myWithdrawals(
        @Query("take") take: Int = 20,
        @Query("skip") skip: Int = 0
    ): Response<WithdrawalListResponse>

    // ── Payments ────────────────────────────────────────────────────────
    @GET("payments/payfast/fund/{dealId}")
    suspend fun getPayFastUrl(@Path("dealId") dealId: String): Response<PayFastUrlResponse>

    @GET("payments/ozow/fund/{dealId}")
    suspend fun getOzowParams(@Path("dealId") dealId: String): Response<OzowParamsResponse>

    // ── Notifications ───────────────────────────────────────────────────
    @GET("notifications")
    suspend fun listNotifications(
        @Query("take") take: Int = 20,
        @Query("skip") skip: Int = 0
    ): Response<NotificationListResponse>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<UnreadCountResponse>

    @PATCH("notifications/read-all")
    suspend fun markAllRead(): Response<Unit>

    @PATCH("notifications/{id}/read")
    suspend fun markOneRead(@Path("id") id: String): Response<Unit>

    @POST("notifications/devices")
    suspend fun registerDeviceToken(@Body request: DeviceTokenRequest): Response<Unit>

    @DELETE("notifications/devices/{token}")
    suspend fun unregisterDeviceToken(@Path("token") token: String): Response<Unit>

    // ── Verification ────────────────────────────────────────────────────
    @GET("verification/me")
    suspend fun getMyVerification(): Response<VerificationStatus>

    @POST("verification")
    suspend fun submitVerification(@Body request: VerificationSubmitRequest): Response<Unit>

    // ── Rate Cards ──────────────────────────────────────────────────────
    @GET("rate-cards")
    suspend fun listRateCards(): Response<List<RateCardResponse>>

    @POST("rate-cards")
    suspend fun createRateCard(@Body request: CreateRateCardRequest): Response<RateCardResponse>

    @PATCH("rate-cards/{id}")
    suspend fun updateRateCard(
        @Path("id") id: String,
        @Body request: CreateRateCardRequest
    ): Response<RateCardResponse>

    @DELETE("rate-cards/{id}")
    suspend fun deleteRateCard(@Path("id") id: String): Response<Unit>

    // ── Collections ─────────────────────────────────────────────────────
    @GET("collections")
    suspend fun listCollections(): Response<List<CollectionResponse>>

    @POST("collections")
    suspend fun createCollection(@Body request: CreateCollectionRequest): Response<CollectionResponse>

    @POST("collections/{id}/items")
    suspend fun addCollectionItem(
        @Path("id") id: String,
        @Body request: CollectionItemRequest
    ): Response<Unit>

    @DELETE("collections/{id}")
    suspend fun deleteCollection(@Path("id") id: String): Response<Unit>

    // ── Analytics ───────────────────────────────────────────────────────
    @POST("analytics")
    suspend fun recordAnalytics(@Body body: Map<String, Any>): Response<Unit>

    @GET("analytics/me")
    suspend fun getMyAnalytics(): Response<AnalyticsSummary>

    // ── Alerts / Saved Searches ─────────────────────────────────────────
    @POST("searches")
    suspend fun createAlert(@Body request: CreateSavedSearchRequest): Response<SavedSearchResponse>

    @GET("searches")
    suspend fun listAlerts(): Response<List<AlertResponse>>

    @PATCH("searches/{id}")
    suspend fun updateAlert(
        @Path("id") id: String,
        @Body body: Map<String, Any>
    ): Response<Unit>

    @DELETE("searches/{id}")
    suspend fun deleteAlert(@Path("id") id: String): Response<Unit>

    // ── Reports ─────────────────────────────────────────────────────────
    @POST("reports/user/{id}")
    suspend fun reportUser(
        @Path("id") id: String,
        @Body request: ReportRequest
    ): Response<Unit>

    @POST("reports/post/{id}")
    suspend fun reportPost(
        @Path("id") id: String,
        @Body request: ReportRequest
    ): Response<Unit>

    @GET("reports/me")
    suspend fun myReports(): Response<List<ReportRequest>>

    // ── Contracts ───────────────────────────────────────────────────────
    @POST("contracts/deals/{dealId}")
    suspend fun generateContract(@Path("dealId") dealId: String): Response<ContractResponse>

    @GET("contracts/deals/{dealId}")
    suspend fun getContract(@Path("dealId") dealId: String): Response<ContractResponse>

    // ── Uploads ─────────────────────────────────────────────────────────
    @Multipart
    @POST("uploads")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody? = null
    ): Response<UploadResponse>

    @GET("uploads/presign")
    suspend fun getPresignedUrl(@Query("key") key: String): Response<Map<String, String>>

    // ── Social login providers actually enabled on the server ──────────
    @GET("social/providers")
    suspend fun socialProviders(): Response<List<SocialProviderResponse>>

    @GET("social/{provider}/start")
    suspend fun socialStart(@Path("provider") provider: String): Response<SocialStartResponse>

    @POST("social/{provider}/exchange")
    suspend fun socialExchange(
        @Path("provider") provider: String,
        @Body request: SocialExchangeRequest
    ): Response<TokenPair>
}
