package org.ergoplatform.desktop.transactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import org.ergoplatform.Application
import org.ergoplatform.SigningSecrets
import org.ergoplatform.desktop.ui.PasswordDialog
import org.ergoplatform.desktop.ui.navigation.NavClientScreenComponent
import org.ergoplatform.desktop.ui.navigation.NavHostComponent
import org.ergoplatform.desktop.ui.proceedAuthFlowWithPassword
import org.ergoplatform.desktop.wallet.addresses.ChooseAddressesListDialog
import org.ergoplatform.transactions.PromptSigningResult
import org.ergoplatform.transactions.TransactionResult
import org.ergoplatform.uilogic.STRING_ERROR_PREPARE_TRANSACTION
import org.ergoplatform.uilogic.STRING_ERROR_SEND_TRANSACTION
import org.ergoplatform.uilogic.transactions.SubmitTransactionUiLogic

abstract class SubmitTransactionComponent(
    val componentContext: ComponentContext,
    private val navHost: NavHostComponent,
) : NavClientScreenComponent(navHost), ComponentContext by componentContext {
    protected abstract val uiLogic: SubmitTransactionUiLogic

    private val passwordDialog = mutableStateOf(false)
    private val chooseAddressDialog = mutableStateOf<Boolean?>(null)
    private val signingPromptDialog = mutableStateOf<String?>(null)

    @Composable
    protected fun SubmitTransactionOverlays() {
        if (passwordDialog.value) {
            PasswordDialog(
                onDismissRequest = { passwordDialog.value = false },
                onPasswordEntered = {
                    proceedAuthFlowWithPassword(
                        it,
                        uiLogic.wallet!!.walletConfig,
                        ::proceedFromAuthFlow
                    )
                }
            )
        }
        if (chooseAddressDialog.value != null) {
            ChooseAddressesListDialog(
                uiLogic.wallet!!,
                chooseAddressDialog.value!!,
                onAddressChosen = { walletAddress ->
                    chooseAddressDialog.value = null
                    onAddressChosen(walletAddress?.derivationIndex)
                },
                onDismiss = { chooseAddressDialog.value = null },
            )
        }
        signingPromptDialog.value?.let {
            SigningPromptDialog(it, uiLogic,
                onContinueClicked = {
                    // TODO
                },
                onDismissRequest = { signingPromptDialog.value = null })
        }
    }

    protected fun showSigningPrompt(signingPrompt: String) {
        signingPromptDialog.value = signingPrompt
    }

    protected fun startChooseAddress(withAllAddresses: Boolean) {
        chooseAddressDialog.value = withAllAddresses
    }

    protected open fun onAddressChosen(derivationIndex: Int?) {
        uiLogic.derivedAddressIdx = derivationIndex
    }

    protected fun startPayment() {
        val walletConfig = uiLogic.wallet!!.walletConfig
        walletConfig.secretStorage?.let {
            passwordDialog.value = true
        } ?: uiLogic.startColdWalletPayment(Application.prefs, Application.texts)
    }

    private fun proceedFromAuthFlow(signingSecrets: SigningSecrets) {
        uiLogic.startPaymentWithMnemonicAsync(
            signingSecrets,
            Application.prefs,
            Application.texts,
            Application.database
        )
    }

    protected fun getTransactionResultErrorMessage(result: TransactionResult): String {
        val errorMsgPrefix = if (result is PromptSigningResult)
            STRING_ERROR_PREPARE_TRANSACTION
        else STRING_ERROR_SEND_TRANSACTION

        return (Application.texts.getString(errorMsgPrefix)
                + (result.errorMsg?.let { "\n\n$it" } ?: ""))
    }

}