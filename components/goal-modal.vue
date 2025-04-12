<template>
	<uni-popup ref="popup" type="center" @change="onPopupChange">
		<view class="modal-container">
			<view class="modal-header">
				<text>{{ modalTitle }}</text>
				<uni-icons type="closeempty" size="20" @click="close" />
			</view>

			<view class="form-container">
				<view class="form-item">
					<text class="form-label">目标名称</text>
					<input v-model="form.name" class="form-input" placeholder="请输入目标名称" maxlength="50" />
				</view>

				<view class="form-item">
					<text class="form-label">目标描述</text>
					<textarea v-model="form.description" class="form-textarea" placeholder="请输入目标描述(可选)"
						maxlength="200" />
				</view>

				<view class="form-row">
					<!-- <view class="form-item">
						<text class="form-label">开始日期</text>
						<uni-datetime-picker v-model="form.startDate" type="date" :clear-icon="false" />
					</view> -->

					<view class="form-item">
						<text class="form-label">截止日期</text>
						<uni-datetime-picker v-model="form.dueDate" type="date" :start="form.startDate"
							:clear-icon="false" />
					</view>
				</view>

				<view class="form-item" v-if="isEditMode">
					<text class="form-label">目标状态</text>
					<uni-data-checkbox v-model="form.status" :localdata="statusOptions" selectedColor="#5b8ef3" />
				</view>
			</view>

			<button class="submit-btn" @click="submit" :disabled="isSubmitting">
				{{ isSubmitting ? '处理中...' : '确认' }}
			</button>
		</view>
	</uni-popup>
</template>

<script>
	import dayjs from 'dayjs';

	export default {
		name: 'GoalModal',
		props: {
			visible: Boolean,
			goal: Object,
			parentId: Number,
			parentStageId: Number
		},
		data() {
			return {
				form: {
					name: '',
					description: '',
					startDate: dayjs().format('YYYY-MM-DD'),
					dueDate: dayjs().add(7, 'day').format('YYYY-MM-DD'),
					status: 0
				},
				statusOptions: [{
						value: 0,
						text: '进行中'
					},
					{
						value: 1,
						text: '已完成'
					}
				],
				isSubmitting: false
			};
		},
		computed: {
			isEditMode() {
				return !!this.goal?.id;
			},
			modalTitle() {
				if (this.isEditMode) {
					return '编辑' + (this.parentId ? '阶段' : '目标');
				}
				return this.parentStageId ? '添加子阶段' :
					this.parentId ? '添加阶段' : '添加新目标';
			}
		},
		watch: {
			visible(newVal) {
				if (newVal) {
					this.$refs.popup.open();

					// 初始化表单数据
					if (this.goal) {
						this.form = {
							name: this.goal.name,
							description: this.goal.description,
							startDate: this.goal.startDate,
							dueDate: this.goal.dueDate,
							status: this.goal.status
						};
					} else {
						this.resetForm();

						// 如果是子目标，设置父目标的截止日期
						if (this.parentId) {
							this.form.dueDate = this.getParentDueDate();
						}
					}
				} else {
					this.$refs.popup.close();
				}
			}
		},
		methods: {
			resetForm() {
				this.form = {
					name: '',
					description: '',
					startDate: dayjs().format('YYYY-MM-DD'),
					dueDate: dayjs().add(7, 'day').format('YYYY-MM-DD'),
					status: 0
				};
			},

			getParentDueDate() {
				// 在实际应用中，这里应该通过Vuex或从父组件传递获取父目标的截止日期
				return dayjs().add(14, 'day').format('YYYY-MM-DD');
			},

			onPopupChange(e) {
				if (!e.show) {
					this.$emit('close');
				}
			},

			close() {
				this.$emit('close');
			},

			async submit() {
				if (!this.validate()) return;

				this.isSubmitting = true;

				try {
					const formData = {
						...this.form,
						startDate: this.formatDate(this.form.startDate),
						dueDate: this.formatDate(this.form.dueDate),
					};

					if (this.isEditMode) {
						formData.id = this.goal.id;
					}

					if (this.parentId && !this.isEditMode) {
						formData.parentId = this.parentId;
					}

					this.$emit('submit', formData);
				} catch (error) {
					console.error('表单提交错误:', error);
				} finally {
					this.isSubmitting = false;
				}
			},

			validate() {
				if (!this.form.name.trim()) {
					uni.showToast({
						title: '请填写目标名称',
						icon: 'none'
					});
					return false;
				}

				if (dayjs(this.form.dueDate).isBefore(this.form.startDate)) {
					uni.showToast({
						title: '截止日期不能早于开始日期',
						icon: 'none'
					});
					return false;
				}

				return true;
			},

			formatDate(date) {
				return dayjs(date).format('YYYY-MM-DD');
			}
		}
	};
</script>

<style scoped>
	.modal-container {
		width: 80vw;
		max-width: 600px;
		background-color: #fff;
		border-radius: 16rpx;
		padding: 32rpx;
	}

	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 32rpx;
		font-size: 18px;
		font-weight: 500;
		color: #333;
	}

	.form-container {
		margin-bottom: 20rpx;
	}

	.form-item {
		margin-bottom: 24rpx;
	}

	.form-label {
		display: block;
		margin-bottom: 8rpx;
		font-size: 14px;
		color: #666;
	}

	.form-input,
	.form-textarea {
		width: 100%;
		padding: 16rpx;
		border: 1px solid #eee;
		border-radius: 8rpx;
		font-size: 14px;
	}

	.form-textarea {
		height: 120rpx;
	}

	.form-row {
		display: flex;
		gap: 20rpx;
	}

	.form-row .form-item {
		flex: 1;
	}

	.submit-btn {
		width: 100%;
		background-color: #5b8ef3;
		color: #fff;
		padding: 20rpx;
		border-radius: 8rpx;
		font-size: 16px;
	}

	.submit-btn:disabled {
		background-color: #b5ceff;
	}
</style>