package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.KolabViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DividerBorder
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawDialog(
    balance: Double,
    viewModel: KolabViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var amountText by remember { mutableStateOf(balance.toInt().toString()) }
    var selectedBank by remember { mutableStateOf("FNB (First National Bank)") }

    val banks = listOf(
        "FNB (First National Bank)",
        "Standard Bank SA",
        "Capitec Bank",
        "Nedbank",
        "Investec"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        scrimColor = PureBlack.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .testTag("withdraw_modal_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Bank",
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Withdraw to Bank",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Available Balance: R %,.2f".format(balance),
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input
            Text(
                text = "Amount (ZAR)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                modifier = Modifier.fillMaxWidth().testTag("withdraw_amount_input"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Text(
                        text = "R",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceContainerHigh,
                    unfocusedContainerColor = SurfaceContainerHigh,
                    focusedBorderColor = PureWhite,
                    unfocusedBorderColor = DividerBorder,
                    focusedTextColor = PureWhite,
                    unfocusedTextColor = PureWhite
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Select Bank
            Text(
                text = "Destination Bank Account",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PureWhite
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                banks.forEach { bank ->
                    val isSelected = selectedBank == bank
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SurfaceContainerHigh else DarkSurface)
                            .border(1.dp, if (isSelected) PureWhite else DividerBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedBank = bank }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = bank,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PureWhite else TextSecondary
                            )
                            if (isSelected) {
                                Text(
                                    text = "•••• 4029",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    viewModel.withdrawFunds(amount)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_withdraw_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                )
            ) {
                Text(
                    text = "Confirm Payout Transfer",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureBlack
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
