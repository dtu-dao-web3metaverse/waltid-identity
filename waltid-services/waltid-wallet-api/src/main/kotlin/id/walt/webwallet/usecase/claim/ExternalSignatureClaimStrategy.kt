package id.walt.webwallet.usecase.claim

import id.walt.webwallet.service.SSIKit2WalletService
import id.walt.webwallet.service.credentials.CredentialsService
import id.walt.webwallet.service.exchange.IssuanceService
import id.walt.webwallet.service.exchange.IssuanceService.OfferedCredentialProofOfPossession
import id.walt.webwallet.usecase.event.EventLogUseCase
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ExternalSignatureClaimStrategy(
    private val issuanceService: IssuanceService,
    private val credentialService: CredentialsService,
    private val eventUseCase: EventLogUseCase,
) {

    suspend fun prepareCredentialClaim(
        did: String,
        keyId: String,
        offerURL: String,
    ) = issuanceService.prepareExternallySignedOfferRequest(
        offerURL = offerURL,
        did = did,
        keyId = keyId,
        credentialWallet = SSIKit2WalletService.getCredentialWallet(did),
    )

    suspend fun submitCredentialClaim(
        tenantId: String,
        accountId: Uuid,
        walletId: Uuid,
        pending: Boolean = true,
        did: String,
        offerURL: String,
        credentialIssuerURL: String,
        accessToken: String?,
        offeredCredentialProofsOfPossession: List<OfferedCredentialProofOfPossession>,
    ) = issuanceService.submitExternallySignedOfferRequest(
        offerURL = offerURL,
        credentialIssuerURL = credentialIssuerURL,
        credentialWallet = SSIKit2WalletService.getCredentialWallet(did),
        offeredCredentialProofsOfPossession = offeredCredentialProofsOfPossession,
        accessToken = accessToken,
    ).map { credentialDataResult ->
        ClaimCommons.convertCredentialDataResultToWalletCredential(
            credentialDataResult,
            walletId,
            pending,
        ).also { credential ->
            ClaimCommons.addReceiveCredentialToUseCaseLog(
                tenantId,
                accountId,
                walletId,
                credential,
                credentialDataResult.type,
                eventUseCase,
            )
        }
    }.also {
        ClaimCommons.storeWalletCredentials(
            walletId,
            it,
            credentialService,
        )
    }
}