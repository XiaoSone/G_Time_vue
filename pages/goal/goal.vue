<template>
	<view class="page">
		<view class="container">
			<!-- 顶部标题和操作按钮 -->
			<view class="header">
				<text class="title">目标管理</text>
				<view class="actions">
					<uni-icons type="plus" size="24" color="#5b8ef3" @click="showAddModal(null)" />
					<uni-icons type="refreshempty" size="24" color="#5b8ef3" @click="fetchGoals" />
				</view>
			</view>

			<!-- 目标树状列表 -->
			<view class="goals-tree">
				<goal-node v-for="goal in goals" :key="goal.id" :node="goal" :depth="0"
					@toggle-stage-status="handleToggleStageStatus" @edit-goal="handleEditGoal"
					@delete-goal="handleDeleteGoal" @delete-stage="handleDeleteStage" @add-sub-goal="showAddModal"
					@add-sub-stage="handleAddSubStage" />
			</view>

			<!-- 空状态 -->
			<view v-if="goals.length === 0" class="empty-state">
				<text class="empty-text">您还没有设置任何目标</text>
				<view class="add-goal-btn" @click="showAddModal(null)">
					<text>添加新目标</text>
				</view>
			</view>

			<!-- 添加/编辑目标模态框 -->
			<goal-modal ref="goalModal" :visible="showModal" :goal="currentGoal" :parent-id="parentId"
				@close="closeModal" @submit="handleSubmitGoal" />
		</view>
	</view>
</template>

<script>
	import axios from 'axios';
	import dayjs from 'dayjs';
	import GoalNode from '../../components/goal-node.vue';
	import GoalModal from '../../components/goal-modal.vue';

	export default {
		components: {
			GoalNode,
			GoalModal
		},
		data() {
			return {
				goals: [],
				showModal: false,
				currentGoal: null,
				parentId: null,
				header: {},
				parentStageId: null
			};
		},
		mounted() {
			this.fetchGoals();
		},
		methods: {
			async fetchGoals() {
				try {
					const token = uni.getStorageSync('token');
					const response = await axios.post('/goals/list', {}, {
						headers: {
							'satoken': token
						}
					});

					this.goals = response.data.data.map(goal => ({
						...goal,
						stages: this.buildStagesTree(goal.stages || []),
						isFolded: true // 第一层默认展开
					}));
				} catch (error) {
					uni.showToast({
						title: '获取目标列表失败',
						icon: 'none'
					});
				}
				console.log(this.goals);
			},

			buildStagesTree(stages, maxDepth = 4, currentDepth = 0) {
				if (!stages || currentDepth >= maxDepth) return [];

				return stages.map(item => ({
					...item,
					// 关键修改：统一将 subStages 转换为 stages
					stages: this.buildStagesTree(item.subStages, maxDepth, currentDepth + 1),
					isExpired: dayjs().isAfter(item.dueDate) && item.status !== 1,
					isFolded: true, // 非第一层默认折叠
					// 保留原始 subStages 字段以防其他逻辑需要
					subStages: undefined
				}));
			},


			showAddModal(parentId, parentStageId = null) {
				this.parentId = parentId;
				this.parentStageId = parentStageId;
				this.currentGoal = null;
				this.showModal = true;
				// 手动调用uni-popup的open方法
				this.$nextTick(() => {
					this.$refs.goalModal.$refs.popup.open();
				});
			},

			handleEditGoal(goal) {
				this.currentGoal = goal;
				this.showModal = true;
			},

			closeModal() {
				this.showModal = false;
			},

			async handleSubmitGoal(formData) {
				const token = uni.getStorageSync('token');
				try {
					if (formData.id) {
						// 更新逻辑
						if (this.currentGoal.goalId) {
							// 更新阶段
							await axios.post(
								`/goals/${this.currentGoal.goalId}/stages/${this.currentGoal.id}/update`, {
									...formData,
									startDate: dayjs(formData.startDate).format('YYYY-MM-DD'),
									dueDate: dayjs(formData.dueDate).format('YYYY-MM-DD')
								}, {
									headers: {
										satoken: token
									}
								}
							);
						} else {
							// 更新目标
							await axios.post(
								`/goals/${this.currentGoal.id}/update`, {
									...formData,
									startDate: dayjs(formData.startDate).format('YYYY-MM-DD'),
									dueDate: dayjs(formData.dueDate).format('YYYY-MM-DD')
								}, {
									headers: {
										satoken: token
									}
								}
							);
						}
					} else {
						// 创建逻辑
						let endpoint = '';
						let requestData = {
							...formData,
							startDate: dayjs(formData.startDate).format('YYYY-MM-DD'),
							dueDate: dayjs(formData.dueDate).format('YYYY-MM-DD')
						};

						if (this.parentStageId) {
							// 添加子阶段
							endpoint = `/goals/${this.parentId}/stages/create`;
							requestData.parentStageId = this.parentStageId;
						} else if (this.parentId) {
							// 添加顶层阶段
							endpoint = `/goals/${this.parentId}/stages/create`;
						} else {
							// 添加目标
							endpoint = '/goals/create';
						}

						await axios.post(endpoint, requestData, {
							headers: {
								'satoken': token
							}
						});
					}

					uni.showToast({
						title: '操作成功',
						icon: 'success'
					});
					this.closeModal();
					this.fetchGoals();
				} catch (error) {
					uni.showToast({
						title: '操作失败',
						icon: 'none'
					});
					console.error('提交失败:', error);
				}
			},
			handleAddSubStage({
				goalId,
				parentStageId
			}) {
				this.showAddModal(goalId, parentStageId);
			},

			async handleToggleStageStatus({
				goalId,
				stageId,
				status
			}) {
				try {
					const token = uni.getStorageSync('token');
					await axios.post(`/goals/${goalId}/stages/${stageId}/status`, {
						status: status ? 1 : 0
					}, {
						headers: {
							'satoken': token
						}
					});
					this.fetchGoals();
				} catch (error) {
					uni.showToast({
						title: '更新状态失败',
						icon: 'none'
					});
				}
			},

			async handleDeleteGoal(goalId) {
				try {
					const token = uni.getStorageSync('token');
					uni.showModal({
						title: '确认删除',
						content: '确定要删除这个目标及其所有子目标吗？',
						success: async (res) => {
							if (res.confirm) {
								await axios.post(`/goals/${goalId}/delete`, {}, {
									headers: {
										'satoken': token
									}
								});
								uni.showToast({
									title: '删除成功',
									icon: 'success'
								});
								this.fetchGoals();
							}
						}
					});
				} catch (error) {
					uni.showToast({
						title: '删除失败',
						icon: 'none'
					});
				}
			},
			async handleDeleteStage({
				goalId,
				stageIds
			}) {
				try {
					console.log(stageIds);
					const token = uni.getStorageSync('token');
					uni.showModal({
						title: '确认删除',
						content: `确定要删除该阶段吗？该阶段下有${stageIds.length - 1}个子节点`, // 显示将删除的阶段数量
						success: async (res) => {
							if (res.confirm) {
								const response = await axios.post(
									`/goals/${goalId}/stages/delete`,
									stageIds, // 直接使用前端收集的stageIds数组
									{
										headers: {
											'satoken': token,
											'Content-Type': 'application/json'
										}
									}
								);

								if (response.data.code === 200) {
									uni.showToast({
										title: '删除成功',
										icon: 'success'
									});
									this.fetchGoals();
								} else {
									throw new Error(response.data.msg || '删除失败');
								}
							}
						}
					});
				} catch (error) {
					console.error('删除阶段失败:', error);
					uni.showToast({
						title: '删除失败: ' + (error.response?.data?.msg || error.message),
						icon: 'none'
					});
				}
			}



		}
	};
</script>

<style scoped>
	.page {
		padding: 20rpx;
		height: 100%;
		background-color: #f8f9fa;
	}

	.container {
		max-width: 100%;
	}

	.header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 30rpx;
		padding: 0 10rpx;
	}

	.title {
		font-size: 20px;
		font-weight: 600;
		color: #333;
	}

	.actions {
		display: flex;
		gap: 20rpx;
	}

	.goals-tree {
		margin-bottom: 100rpx;
	}

	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 0;
	}

	.empty-image {
		width: 200rpx;
		height: 200rpx;
		margin-bottom: 30rpx;
		opacity: 0.6;
	}

	.empty-text {
		font-size: 16px;
		color: #999;
		margin-bottom: 30rpx;
	}

	.add-goal-btn {
		background-color: #5b8ef3;
		color: #fff;
		padding: 16rpx 40rpx;
		border-radius: 30rpx;
		font-size: 14px;
	}
</style>