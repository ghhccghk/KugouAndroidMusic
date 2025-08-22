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
            .padding(7.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "酷狗VIP：${if (vipData.isVip == 1) "未到期" else "已到期"},有效期至：${vipData.vipEndTime}", style = MaterialTheme.typography.titleMedium)
                Text("VIP 等级:${vipData.svipLevel}, 积分:${vipData.svipScore}")
            }
            vipData.busiVip.forEach { vip ->
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${if (vip.productType.uppercase() == "SVIP") "概念SVIP" else "概念畅听VIP"}${if (vip.isVip == 1) "：未到期" else "已到期"}，有效期至：${vip.vipEndTime}")
                    vip.vipLimitQuota?.total?.let {
                        Text("配额：$it")
                    }
                }

            }
        }


    }
}
