package com.ghhccghk.musicplay.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ghhccghk.musicplay.data.user.vipdata.VipData

@Composable
fun VipInfoScreen(vipData: VipData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "全局 VIP 状态: ${if (vipData.isVip == 1) "有效" else "无效"}", style = MaterialTheme.typography.titleMedium)
                Text("全局有效期: ${vipData.vipBeginTime} ~ ${vipData.vipEndTime}")
                Text("SVIP 等级: ${vipData.svipLevel}, 积分: ${vipData.svipScore}")
            }
        }

        vipData.busiVip.forEach { vip ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("类型: ${vip.productType.uppercase()}")
                    Text("状态: ${if (vip.isVip == 1) "有效" else "无效"}")
                    Text("有效期: ${vip.vipBeginTime} ~ ${vip.vipEndTime}")
                    vip.vipLimitQuota?.total?.let {
                        Text("配额: $it")
                    }
                }
            }
        }
    }
}
