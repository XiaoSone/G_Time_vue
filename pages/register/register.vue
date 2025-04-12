<template>
	<view class="page">
		<view class="container">
			<!-- Logo区域 -->
			<view class="logo-area">
				<uni-icons type="clock" size="60" color="#64D8BE" />
				<text class="app-name">TimeFlow</text>
				<text class="app-slogan">开启高效生活</text>
			</view>

			<!-- 注册表单 -->
			<view class="form-area">
				<view class="input-group">
					<uni-icons type="person" size="20" color="#8F9BB3" />
					<input type="text" v-model="nickname" placeholder="请输入昵称" class="input" />
				</view>

				<view class="input-group">
					<uni-icons type="email" size="20" color="#8F9BB3" />
					<input type="text" v-model="contact" placeholder="手机号/邮箱" class="input" />
				</view>

				<view class="input-group">
					<uni-icons type="locked" size="20" color="#8F9BB3" />
					<input :type="showPassword ? 'text' : 'password'" v-model="password" placeholder="请输入密码"
						class="input" />
					<uni-icons :type="showPassword ? 'eye-filled' : 'eye-slash-filled'" size="20" color="#8F9BB3"
						class="cursor-pointer" @click="togglePassword" />
				</view>

				<view class="input-group">
					<uni-icons type="locked" size="20" color="#8F9BB3" />
					<input :type="showConfirmPassword ? 'text' : 'password'" v-model="confirmPassword"
						placeholder="请确认密码" class="input" />
					<uni-icons :type="showConfirmPassword ? 'eye-filled' : 'eye-slash-filled'" size="20" color="#8F9BB3"
						class="cursor-pointer" @click="toggleConfirmPassword" />
				</view>

				<button class="register-btn cursor-pointer" @click="handleRegister">注册</button>
			</view>

			<!-- 登录链接 -->
			<text class="login-link cursor-pointer" @click="goToLogin">已有账号，返回登录</text>

			<!-- 底部链接 -->
			<view class="bottom-links">
				<text class="privacy-link cursor-pointer" @click="showPrivacyPolicy">隐私政策</text>
			</view>
		</view>
	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import axios from 'axios';

export default defineComponent({
  data() {
    return {
      nickname: '',          // 昵称
      contact: '',           // 联系方式（手机号或邮箱）
      password: '',          // 密码
      confirmPassword: '',   // 确认密码
      showPassword: false,   // 是否显示密码
      showConfirmPassword: false, // 是否显示确认密码
      emailRegex: /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/, // 邮箱正则
      phoneRegex: /^1[3-9]\d{9}$/ // 手机号正则
    };
  },
  methods: {
    // 切换密码可见性
    togglePassword() {
      this.showPassword = !this.showPassword;
    },
    // 切换确认密码可见性
    toggleConfirmPassword() {
      this.showConfirmPassword = !this.showConfirmPassword;
    },
    // 处理注册逻辑
    async handleRegister() {
      if (!this.nickname) {
        uni.showToast({ title: '请输入昵称', icon: 'none' });
        return;
      }
      if (!this.contact) {
        uni.showToast({ title: '请输入手机号或邮箱', icon: 'none' });
        return;
      }
      if (!this.password) {
        uni.showToast({ title: '请输入密码', icon: 'none' });
        return;
      }
      if (this.password !== this.confirmPassword) {
        uni.showToast({ title: '两次输入的密码不一致', icon: 'none' });
        return;
      }
      if (!this.phoneRegex.test(this.contact) && !this.emailRegex.test(this.contact)) {
        uni.showToast({
          title: '手机号或邮箱格式有误，请检查后重新输入',
          icon: 'none'
        });
        return;
      }

      const phone = this.phoneRegex.test(this.contact) ? this.contact : '';
      const email = this.emailRegex.test(this.contact) ? this.contact : '';

      try {
        const response = await axios.post('/user/register', {
          username: this.nickname,
          password: this.confirmPassword,
          email: email,
          phone: phone
        });
        if (response.data.code == 200) {
          uni.showToast({ title: '注册成功', icon: 'success' });
          uni.navigateTo({
            url: '/pages/login/login'
          });
        } else {
          uni.showToast({
            title: response.data.msg || '登录失败',
            icon: 'none'
          });
        }
      } catch (error) {
        uni.showToast({
          title: '请求失败，请检查网络连接',
          icon: 'none'
        });
        console.error('请求失败:', error);
      }
    },

    goToLogin() {
      uni.navigateBack();
    },
	
    showPrivacyPolicy() {
      uni.showModal({
        title: '隐私政策',
        content: '感谢您使用TimeFlow。我们非常重视您的个人信息和隐私保护...',
        showCancel: false,
        confirmText: '我知道了'
      });
    }
  }
});
</script>


<style>
	page {
		height: 100%;
	}

	.page {
		height: 100%;
		background-color: #FFFFFF;
	}

	.container {
		min-height: 100%;
		padding: 60rpx 40rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.logo-area {
		display: flex;
		flex-direction: column;
		align-items: center;
		margin-bottom: 80rpx;
	}

	.app-name {
		font-size: 36px;
		font-weight: 600;
		color: #2C3E50;
		margin-top: 20rpx;
	}

	.app-slogan {
		font-size: 14px;
		color: #8F9BB3;
		margin-top: 10rpx;
	}

	.form-area {
		width: 100%;
		margin-bottom: 40rpx;
	}

	.input-group {
		display: flex;
		align-items: center;
		padding: 24rpx 32rpx;
		background-color: #F7F9FC;
		border-radius: 16rpx;
		margin-bottom: 24rpx;
	}

	.input {
		flex: 1;
		margin-left: 20rpx;
		font-size: 14px;
		color: #2C3E50;
	}

	.register-btn {
		width: 100%;
		height: 88rpx;
		line-height: 88rpx;
		background-color: #64D8BE;
		color: #FFFFFF;
		font-size: 16px;
		border-radius: 44rpx;
		margin-top: 40rpx;
	}

	.login-link {
		color: #64D8BE;
		font-size: 14px;
		margin-bottom: 40rpx;
	}

	.bottom-links {
		margin-top: 40rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.privacy-link {
		color: #8F9BB3;
		font-size: 12px;
	}

	.cursor-pointer {
		cursor: pointer;
	}
</style>