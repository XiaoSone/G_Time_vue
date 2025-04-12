<template>
	<view class="page">
		<view class="profile-container">
			<!-- 个人信息区域 -->
			<view class="profile-header">
				<view class="avatar-container">
					<image class="avatar" :src="userInfo.avatar" mode="aspectFill" />
					<view class="edit-avatar cursor-pointer" @click="handleEditAvatar">
						<uni-icons type="camera-filled" size="20" color="#FFFFFF" />
					</view>
				</view>
				<view class="name-container">
					<text class="name">{{ userInfo.name }}</text>
					<uni-icons type="compose" size="16" color="#666666" class="edit-icon cursor-pointer"
						@click="handleEditName" />
				</view>
				<view class="intro-container">
					<text class="intro">{{ userInfo.intro }}</text>
					<uni-icons type="compose" size="16" color="#666666" class="edit-icon cursor-pointer"
						@click="handleEditIntro" />
				</view>
			</view>

			<!-- 基本信息区域 -->
			<view class="info-section">
				<view class="info-item">
					<text class="info-label">邮箱</text>
					<view class="info-value">
						<text>{{ userInfo.email }}</text>
						<uni-icons type="checkmarkempty" size="16" color="#4CD964" />
					</view>
				</view>
				<view class="info-item">
					<text class="info-label">手机号</text>
					<text class="info-value">{{ userInfo.phone }}</text>
				</view>
			</view>

			<!-- 功能列表区域 -->
			<view class="function-list">
				<view class="function-item cursor-pointer" @click="handleSecurity">
					<view class="item-left">
						<uni-icons type="locked" size="20" color="#36B4FE" />
						<text class="item-text">账户安全</text>
					</view>
					<uni-icons type="right" size="16" color="#CCCCCC" />
				</view>
				<view class="function-item cursor-pointer" @click="handlePassword">
					<view class="item-left">
						<image src="../../static/my/密码修改.png" mode="aspectFit" ></image>
						<text class="item-text">密码修改</text>
					</view>
					<uni-icons type="right" size="16" color="#CCCCCC" />
				</view>
				<view class="function-item cursor-pointer" @click="handleNotification">
					<view class="item-left">
						<uni-icons type="notification" size="20" color="#9C7AFE" />
						<text class="item-text">通知设置</text>
					</view>
					<uni-icons type="right" size="16" color="#CCCCCC" />
				</view>
				<view class="function-item cursor-pointer" @click="handlePrivacy">
					<view class="item-left">
						<image src="../../static/my/隐私设置.png" mode="aspectFit" ></image>
						<text class="item-text">隐私设置</text>
					</view>
					<uni-icons type="right" size="16" color="#CCCCCC" />
				</view>
				<view class="function-item cursor-pointer" @click="handleHelp">
					<view class="item-left">
						<uni-icons type="help" size="20" color="#8A8A8A" />
						<text class="item-text">帮助中心</text>
					</view>
					<uni-icons type="right" size="16" color="#CCCCCC" />
				</view>
				<view class="function-item cursor-pointer" @click="handleAbout">
					<view class="item-left">
						<uni-icons type="info" size="20" color="#8A8A8A" />
						<text class="item-text">关于我们</text>
					</view>
					<uni-icons type="right" size="16" color="#CCCCCC" />
				</view>
			</view>

			<!-- 退出登录按钮 -->
			<button class="logout-btn cursor-pointer" @click="handleLogout">退出登录</button>
		</view>

		<!-- 编辑弹窗 -->
		<uni-popup ref="popup" type="dialog">
			<uni-popup-dialog :title="dialogTitle" :content="dialogContent" :value="dialogValue"
				@confirm="handleDialogConfirm" @close="handleDialogClose" />
		</uni-popup>
	</view>
</template>

<script lang="ts">
	import axios from 'axios';
	import { defineComponent } from 'vue';
	import { ref, reactive } from 'vue';

	export default defineComponent({
		data() {
			return {
				popup: ref(),
				dialogTitle: ref(''),
				dialogContent: ref(''),
				dialogValue: ref(''),
				currentEditType: ref(''),
				userInfo: reactive({
					avatar: "../../static/默认头像1.png",
					name: '李小明',
					intro: '热爱生活，专注时间管理，让每一天都过得更有意义。',
					email: '暂未绑定',
					phone: '暂未绑定'
				})
			};
		},
		methods: {
			handleEditAvatar() {
				uni.showToast({
					title: '编辑头像功能开发中',
					icon: 'none'
				});
			},
			handleEditName() {
				this.dialogTitle.value = '修改昵称';
				this.dialogContent.value = '请输入新的昵称';
				this.dialogValue.value = this.userInfo.name;
				this.currentEditType.value = 'name';
				this.popup.value.open();
			},
			handleEditIntro() {
				this.dialogTitle.value = '修改简介';
				this.dialogContent.value = '请输入新的简介';
				this.dialogValue.value = this.userInfo.intro;
				this.currentEditType.value = 'intro';
				this.popup.value.open();
			},
			handleDialogConfirm(value : string) {
				if (this.currentEditType.value === 'name') {
					this.userInfo.name = value;
				} else if (this.currentEditType.value === 'intro') {
					this.userInfo.intro = value;
				}
				uni.showToast({
					title: '保存成功',
					icon: 'success'
				});
				this.popup.value.close();
			},
			handleDialogClose() {
				this.popup.value.close();
			},
			handleSecurity() {
				uni.showToast({
					title: '账户安全功能开发中',
					icon: 'none'
				});
			},
			handlePassword() {
				uni.showToast({
					title: '密码修改功能开发中',
					icon: 'none'
				});
			},
			handleNotification() {
				uni.showToast({
					title: '通知设置功能开发中',
					icon: 'none'
				});
			},
			handlePrivacy() {
				uni.showToast({
					title: '隐私设置功能开发中',
					icon: 'none'
				});
			},
			handleHelp() {
				uni.showToast({
					title: '帮助中心功能开发中',
					icon: 'none'
				});
			},
			handleAbout() {
				uni.showToast({
					title: '关于我们功能开发中',
					icon: 'none'
				});
			},
			handleLogout() {
				uni.showModal({
					title: '提示',
					content: '确定要退出登录吗？',
					success: (res) => {
						if (res.confirm) {
							// 获取本地存储的 token
							const token = uni.getStorageSync('token');
							if (!token) {
								uni.showToast({
									title: '未找到登录信息',
									icon: 'none'
								});
								return;
							}

							// 发送退出登录请求，将 token 添加到 header 中
							axios({
								url: '/user/logout',
								method: 'POST',
								headers: {
									'satoken': token
								}
							})
								.then(() => {
									uni.showToast({
										title: '已退出登录',
										icon: 'success'
									});
									// 清除本地存储的 token并跳转至login页
									uni.removeStorageSync('token');
									uni.removeStorageSync('user');
									uni.navigateTo({
										url: '/pages/login/login'
									})
								})
								.catch((err) => {
									console.error('退出登录失败:', err);
									uni.showToast({
										title: '退出登录失败',
										icon: 'none'
									});
								});
						}
					}
				});
			}


		},
		created() {
			this.userInfo = uni.getStorageSync('user')
		}
	});
</script>


<style>
	page {
		height: 100%;
		background-color: #F8F8F8;
	}

	.profile-container {
		padding: 40rpx 30rpx;
	}

	.profile-header {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 40rpx;
		background-color: #FFFFFF;
		border-radius: 16rpx;
		margin-bottom: 20rpx;
	}

	.avatar-container {
		position: relative;
		margin-bottom: 20rpx;
	}

	.avatar {
		width: 160rpx;
		height: 160rpx;
		border-radius: 80rpx;
	}

	.edit-avatar {
		position: absolute;
		right: 0;
		bottom: 0;
		width: 40rpx;
		height: 40rpx;
		background-color: #36B4FE;
		border-radius: 20rpx;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.name-container {
		display: flex;
		align-items: center;
		margin-bottom: 16rpx;
	}

	.name {
		font-size: 18px;
		font-weight: 500;
		color: #333333;
		margin-right: 12rpx;
	}

	.intro-container {
		display: flex;
		align-items: center;
	}

	.intro {
		font-size: 14px;
		color: #666666;
		margin-right: 12rpx;
	}

	.edit-icon {
		padding: 8rpx;
	}

	.info-section {
		background-color: #FFFFFF;
		border-radius: 16rpx;
		padding: 30rpx;
		margin-bottom: 20rpx;
	}

	.info-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 20rpx 0;
	}

	.info-item:first-child {
		padding-top: 0;
	}

	.info-item:last-child {
		padding-bottom: 0;
	}

	.info-label {
		font-size: 14px;
		color: #999999;
	}

	.info-value {
		font-size: 14px;
		color: #333333;
		display: flex;
		align-items: center;
	}

	.info-value text {
		margin-right: 8rpx;
	}

	.function-list {
		background-color: #FFFFFF;
		border-radius: 16rpx;
		padding: 0 30rpx;
	}

	.function-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 30rpx 0;
		border-bottom: 1px solid #F5F5F5;
	}

	.function-item:last-child {
		border-bottom: none;
	}

	.item-left {
		display: flex;
		align-items: center;
	}
	.item-left image{
		width: 20px;
		height: 20px;
	}

	.item-text {
		font-size: 14px;
		color: #333333;
		margin-left: 16rpx;
	}

	.logout-btn {
		margin-top: 60rpx;
		width: 100%;
		height: 88rpx;
		line-height: 88rpx;
		background-color: #FEE7E7;
		color: #FF5151;
		font-size: 16px;
		border-radius: 44rpx;
		border: none;
	}

	.logout-btn::after {
		border: none;
	}

	.cursor-pointer {
		cursor: pointer;
	}
</style>