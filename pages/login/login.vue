<template>
	<view class="login-container">

		<view class="brand-area">
			<view class="logo-wrapper">
				<uni-icons type="clock" size="40" color="#64D9B9"></uni-icons>
			</view>
			<view class="brand-title">TimeFlow</view>
			<view class="brand-slogan">开启高效生活</view>
		</view>

		<!-- 登录表单区域 -->
		<view class="form-area">
			<view class="input-wrapper">
				<uni-icons type="email" size="20" color="#B2B2B2"></uni-icons>
				<input type="text" v-model="loginForm.account" placeholder="手机号/邮箱"
					placeholder-class="placeholder-style" />
			</view>

			<view class="input-wrapper">
				<uni-icons type="locked" size="20" color="#B2B2B2"></uni-icons>
				<input :type="showPassword ? 'text' : 'password'" v-model="loginForm.password" placeholder="密码"
					placeholder-class="placeholder-style" />
				<view class="eye-icon cursor-pointer" @click="togglePasswordVisibility">
					<uni-icons :type="showPassword ? 'eye' : 'eye-slash'" size="20" color="#B2B2B2"></uni-icons>
				</view>
			</view>

			<view class="forgot-password cursor-pointer" @click="handleForgotPassword">
				忘记密码?
			</view>

			<button class="login-btn cursor-pointer" @click="handleLogin">登录</button>
		</view>

		<!-- 第三方登录区域 -->
		<view class="third-party-login">
			<view class="divider">
				<text class="divider-text">或</text>
			</view>

			<view class="login-options">
				<view class="login-option cursor-pointer" @click="handleWechatLogin">
					<image src="../../static/icon32_wx_logo.png" mode="aspectFit"></image>
					<text>微信登录</text>
				</view>

				<view class="login-option cursor-pointer" @click="handleAppleLogin">
					<image src="../../static/iphone.png" mode="aspectFit"></image>
					<text>Apple 登录</text>
				</view>

				<view class="login-option cursor-pointer" @click="handleGoogleLogin">
					<image src="../../static/google.png" mode="aspectFit"></image>
					<text>Google 登录</text>
				</view>
			</view>
		</view>

		<!-- 注册入口 -->
		<view class="register-entry">
			<text>还没有账号? </text>
			<text class="register-link cursor-pointer" @click="handleRegister">立即注册</text>
		</view>
	</view>
</template>

<script lang="ts">
	import { defineComponent } from 'vue';
	import axios from 'axios';

	export default defineComponent({
		data() {
			return {
				showPassword: false,
				loginForm: {
					account: '',
					password: ''
				}
			};
		},
		methods: {
			togglePasswordVisibility() {
				this.showPassword = !this.showPassword;
			},
			async handleLogin() {
				if (!this.loginForm.account || !this.loginForm.password) {
					uni.showToast({
						title: '请填写完整登录信息',
						icon: 'none'
					});
					return;
				}
				const emailRegex = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
				const phoneRegex = /^1[3-9]\d{9}$/;
				if (!emailRegex.test(this.loginForm.account) && !phoneRegex.test(this.loginForm.account)) {
					uni.showToast({
						title: '请输入有效的手机号或邮箱',
						icon: 'none'
					});
					return;
				}

				try {
					// 发送登录请求
					const response = await axios.post('/user/login', {
						loginName: this.loginForm.account,
						loginPaw: this.loginForm.password
					});

					if (response.data.code === 200) {
						const token = response.data.data.token;
						const userInfo = {
							name: response.data.data.user.username,
							email: response.data.data.user.email,
							phone: response.data.data.user.phone,
							intro: response.data.data.user.intro,
							avatar: response.data.data.user.avatar,
							status: response.data.data.user.status
						};
						uni.setStorageSync('token', token);

						uni.setStorageSync('user', userInfo);

						// 登录成功，跳转到主页
						uni.showToast({
							title: '登录成功',
							icon: 'success'
						});

						uni.switchTab({
							url: '/pages/index/index'
						});
					} else {
						// 登录失败，提示错误信息
						uni.showToast({
							title: response.data.msg || '登录失败',
							icon: 'none'
						});
					}
				} catch (error) {
					// 请求失败，提示错误信息
					uni.showToast({
						title: '请求失败，请检查网络连接',
						icon: 'none'
					});
					console.error('请求失败:', error);
				}
			},
			handleForgotPassword() {
				uni.showToast({
					title: '请通过邮箱重置密码',
					icon: 'none'
				});
			},
			handleWechatLogin() {
				uni.showToast({
					title: '微信登录',
					icon: 'none'
				});
			},
			handleAppleLogin() {
				uni.showToast({
					title: 'Apple登录',
					icon: 'none'
				});
			},
			handleGoogleLogin() {
				uni.showToast({
					title: 'Google登录',
					icon: 'none'
				});
			},
			handleRegister() {
				uni.navigateTo({
					url: '/pages/register/register'
				});
			}
		}
	});
</script>


<style>
	page {
		height: 100%;
	}

	.login-container {
		min-height: 100%;
		background-color: #FFFFFF;
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 80rpx 40rpx;
		box-sizing: border-box;
	}

	.brand-area {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-bottom: 60rpx;
	}

	.logo-wrapper {
		width: 120rpx;
		height: 120rpx;
		background-color: rgba(100, 217, 185, 0.1);
		border-radius: 30rpx;
		display: flex;
		justify-content: center;
		align-items: center;
		margin-bottom: 20rpx;
	}

	.brand-title {
		font-size: 32px;
		color: #333333;
		font-weight: 600;
		margin-bottom: 10rpx;
	}

	.brand-slogan {
		font-size: 14px;
		color: #999999;
	}

	.form-area {
		width: 100%;
		margin-bottom: 60rpx;
	}

	.input-wrapper {
		display: flex;
		align-items: center;
		height: 100rpx;
		background-color: #F8F8F8;
		border-radius: 16rpx;
		padding: 0 30rpx;
		margin-bottom: 20rpx;
	}

	.input-wrapper uni-icons {
		margin-right: 20rpx;
	}

	.input-wrapper input {
		flex: 1;
		height: 100%;
		font-size: 14px;
		color: #333333;
	}

	.placeholder-style {
		color: #B2B2B2;
	}

	.eye-icon {
		padding: 20rpx;
	}

	.forgot-password {
		text-align: right;
		font-size: 14px;
		color: #64D9B9;
		margin: 20rpx 0 40rpx;
	}

	.login-btn {
		width: 100%;
		height: 90rpx;
		background-color: #64D9B9;
		color: #FFFFFF;
		font-size: 16px;
		border-radius: 45rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		border: none;
	}

	.third-party-login {
		width: 100%;
		margin-top: 60rpx;
	}

	.divider {
		position: relative;
		text-align: center;
		margin-bottom: 60rpx;
	}

	.divider::before,
	.divider::after {
		content: '';
		position: absolute;
		top: 50%;
		width: 40%;
		height: 1px;
		background-color: #EEEEEE;
	}

	.divider::before {
		left: 0;
	}

	.divider::after {
		right: 0;
	}

	.divider-text {
		display: inline-block;
		padding: 0 20rpx;
		color: #999999;
		font-size: 14px;
		background-color: #FFFFFF;
	}

	.login-options {
		display: flex;
		flex-direction: column;
		gap: 30rpx;
	}

	.login-option {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 20rpx;
		height: 90rpx;
		border-radius: 45rpx;
		border: 1px solid #EEEEEE;
	}

	.login-option image {
		width: 40rpx;
		height: 40rpx;
	}

	.login-option text {
		font-size: 14px;
		color: #333333;
	}

	.register-entry {
		margin-top: 60rpx;
		font-size: 14px;
		color: #999999;
	}

	.register-link {
		color: #64D9B9;
	}

	.cursor-pointer {
		cursor: pointer;
	}
</style>