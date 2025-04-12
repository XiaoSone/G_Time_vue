<template>
	<view class="node-container">
		<!-- 当前节点 -->
		<view class="goal-card" @click.stop="toggleFold" :class="{ 
				'card-expired': node.isExpired,
				'card-completed': node.status === 1
			}">
			<view class="goal-header">
				<!-- 移除了折叠图标的条件，改为总是显示 -->
				<uni-icons :type="node.isFolded ? 'down' : 'up'" size="16" color="#999" />
				<view class="radio-button" :class="{ 'radio-checked': node.status === 1 }" v-if="node.goalId"
					@click.stop="toggleStatus">
					<view class="radio-inner" v-if="node.status === 1"></view>
				</view>

				<text class="goal-title">{{ node.name }}</text>

				<view class="goal-meta">
					<text class="goal-date">{{ formatDate(node.dueDate) }}</text>
					<text class="goal-status" :class="statusClass">
						{{ statusText }}
					</text>
				</view>

				<view class="goal-actions">
					<uni-icons type="compose" size="16" color="#999" @click="editGoal" />
					<uni-icons type="trash" size="16" color="#ff6b6b" @click="deleteGoal" />
				</view>
			</view>

			<view class="progress-container">
				<text class="progress-text">{{ progressDisplay }}%</text>
				<view class="progress-bar">
					<view class="progress-fill" :style="{ width: `${progressDisplay}%` }" :class="{ 
			      'fill-completed': node.status === 1,
			      'fill-expired': node.isExpired && node.status !== 1
			    }"></view>
				</view>
			</view>

			<text class="goal-description" v-if="node.description">
				{{ node.description }}
			</text>

			<!-- 添加子节点按钮（始终显示） -->
			<view class="add-child-btn" @click.stop="addChildNode">
				<uni-icons type="plusempty" size="14" color="#999" />
				<text>{{ node.goalId ? '添加子阶段' : '添加阶段' }}</text>
			</view>
		</view>

		<!-- 子节点 -->
		<view class="children-container" v-show="!node.isFolded && hasChildren">
			<view class="children-wrapper">
				<goal-node v-for="child in node.stages" :key="child.id" :node="child"
					@toggle-stage-status="$emit('toggle-stage-status', $event)" @edit-goal="$emit('edit-goal', $event)"
					@delete-goal="$emit('delete-goal', $event)" @delete-stage="$emit('delete-stage', $event)"
					@add-sub-goal="$emit('add-sub-goal', $event)" @add-sub-stage="$emit('add-sub-stage', $event)" />
			</view>
		</view>
	</view>
</template>

<script>
	import dayjs from 'dayjs';

	export default {
		name: 'GoalNode',
		props: {
			node: {
				type: Object,
				required: true
			},
		},
		data() {
			return {
				childrenHeight: 0
			};
		},
		mounted() {
			this.$nextTick(() => {
				this.updateChildrenHeight();
			});
		},
		updated() {
			this.updateChildrenHeight();
		},
		computed: {
			hasChildren() {
				return this.node.stages && this.node.stages.length > 0;
			},
			statusText() {
				if (this.node.isExpired) return '已过期';
				return this.node.status === 1 ? '已完成' : '进行中';
			},
			statusClass() {
				return {
					'status-expired': this.node.isExpired,
					'status-completed': this.node.status === 1
				};
			},
			calculatedProgress() {
				// 如果没有子节点，根据status决定进度
				if (!this.hasChildren) {
					return this.node.status === 1 ? 100 : 0;
				}

				// 有子节点的情况，递归计算
				const children = this.node.stages || [];
				if (children.length === 0) return 0;

				// 计算所有子节点的总进度
				const totalProgress = children.reduce((sum, child) => {
					// 对于子节点也使用相同的计算逻辑
					const childProgress = child.progress !== undefined ?
						child.progress :
						this.calculateChildProgress(child);
					return sum + childProgress;
				}, 0);

				// 返回平均进度
				return Math.round(totalProgress / children.length);
			},
			progressDisplay() {
				// 如果节点已完成，直接显示100%
				if (this.node.status === 1) return 100;
				// 否则使用原来的计算方式
				return this.calculatedProgress;
			},
		},
		watch: {
			// 更精准地监视进度变化
			'node.stages': {
				deep: true,
				handler() {
					this.checkAutoComplete();
					// this.updateChildrenHeight();
				}
			},
			progressDisplay: {
				handler(newVal) {
					this.checkAutoComplete();
				}
			}
		},
		methods: {
			updateChildrenHeight() {
				if (this.hasChildren && this.$refs.childrenWrapper) {
					this.$nextTick(() => {
						const query = uni.createSelectorQuery().in(this);
						query.select('.children-wrapper').boundingClientRect(data => {
							if (data) {
								this.childrenHeight = data.height;
							}
						}).exec();
					});
				}
			},
			formatDate(date) {
				return dayjs(date).format('MM/DD');
			},
			toggleFold() {
				// 保持原有功能，但不再检查是否有子节点
				if (typeof uni !== 'undefined' && uni.vibrateShort) {
					uni.vibrateShort();
				}

				const newFoldState = !this.node.isFolded;
				this.$set(this.node, 'isFolded', newFoldState);

				this.$emit('update:node', {
					...this.node,
					isFolded: newFoldState
				});

				if (!newFoldState) {
					this.$nextTick(() => {
						this.scrollIntoViewIfNeeded();
					});
				}
			},
			editGoal() {
				this.$emit('edit-goal', this.node);
			},
			deleteGoal(e) {
				// 确保有正确的 stageIds
				let stageIds = [];
				if (this.node.goalId) {
					// 是阶段节点 - 收集所有需要删除的ID
					stageIds = this.collectStageIds(this.node);
					console.log('将要删除的阶段IDs:', stageIds); // 调试日志

					// 确保stageIds是有效的数组
					if (!Array.isArray(stageIds) || stageIds.length === 0) {
						uni.showToast({
							title: '未找到要删除的阶段',
							icon: 'none'
						});
						return;
					}

					this.$emit('delete-stage', {
						goalId: this.node.goalId,
						stageIds: stageIds
					});
				} else {
					// 是目标节点
					this.$emit('delete-goal', this.node.id);
				}
			},
			// 递归收集所有子阶段ID
			collectStageIds(node) {
				if (!node) return [];
				let ids = [node.id];
				if (node.stages && node.stages.length > 0) {
					node.stages.forEach(child => {
						ids = ids.concat(this.collectStageIds(child));
					});
				}
				return ids;
			},
			calculateChildProgress(child) {
				const hasChildren = child.stages && child.stages.length > 0;
				if (!hasChildren) {
					return child.status === 1 ? 100 : 0;
				}

				const children = child.stages || [];
				if (children.length === 0) return 0;

				const total = children.reduce((sum, grandChild) => {
					return sum + this.calculateChildProgress(grandChild);
				}, 0);

				return Math.round(total / children.length);
			},
			toggleStatus() {
				if (this.hasChildren && this.node.status !== 1) {
					const hasIncompleteChildren = this.checkIncompleteChildren(this.node);
					if (hasIncompleteChildren) {
						uni.showModal({
							title: '确认完成',
							content: '该节点包含未完成的子节点，确定要标记为已完成吗？',
							success: (res) => {
								if (res.confirm) {
									this.emitStatusChange();
								}
							}
						});
						return;
					}
				}
				this.emitStatusChange();
			},
			emitStatusChange() {
				this.$emit('toggle-stage-status', {
					goalId: this.node.goalId || this.node.id,
					stageId: this.node.id,
					status: this.node.status !== 1
				});
			},
			checkIncompleteChildren(node) {
				if (!node.stages || node.stages.length === 0) return false;
				return node.stages.some(child => {
					if (child.status !== 1) return true;
					return this.checkIncompleteChildren(child);
				});
			},
			scrollIntoViewIfNeeded() {
				const query = uni.createSelectorQuery().in(this);
				query.select('.goal-card').boundingClientRect(data => {
					if (data) {
						uni.pageScrollTo({
							duration: 300,
							scrollTop: data.top - 20
						});
					}
				}).exec();
			},
			addChildNode() {
				if (this.node.goalId) {
					// 如果是阶段节点，触发添加子阶段
					this.$emit('add-sub-stage', {
						goalId: this.node.goalId,
						parentStageId: this.node.id
					});
				} else {
					// 如果是目标节点，触发添加子目标
					this.$emit('add-sub-goal', this.node.id);
				}
			}

		}
	};
</script>

<style scoped>
	.node-container {
		margin-bottom: 10rpx;
	}

	.radio-button {
		width: 16px;
		height: 16px;
		border-radius: 50%;
		border: 1px solid #000000;
		position: relative;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-left: 15rpx;
	}

	.radio-checked {
		border-color: #5b8ef3;
		background-color: #5b8ef3;
	}

	.radio-inner {
		width: 8px;
		height: 8px;
		border-radius: 50%;
		background-color: white;
	}

	.goal-card {
		background-color: #fff;
		border-radius: 12rpx;
		padding: 20rpx;
		margin-bottom: 2px;
		box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
		border-left: 6rpx solid #5b8ef3;
	}

	.card-expired {
		border-left-color: #ff6b6b !important;
		opacity: 0.9;
	}

	.card-completed {
		border-left-color: #a8e6cf !important;
	}

	.goal-header {
		display: flex;
		align-items: center;
		margin-bottom: 16rpx;
	}

	.goal-title {
		flex: 1;
		font-size: 16px;
		color: #333;
		margin-left: 10rpx;
		margin-right: 10rpx;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.goal-meta {
		display: flex;
		align-items: center;
		margin-left: auto;
	}

	.goal-date {
		font-size: 12px;
		color: #999;
		margin-right: 12rpx;
	}

	.goal-status {
		font-size: 12px;
	}

	.status-expired {
		color: #ff6b6b;
	}

	.status-completed {
		color: #a8e6cf;
	}

	.goal-actions {
		display: flex;
		margin-left: 15rpx;
		gap: 10rpx;
	}

	.progress-container {
		display: flex;
		align-items: center;
		margin-top: 15rpx;
	}

	.progress-text {
		width: 60rpx;
		font-size: 12px;
		color: #666;
		margin-right: 12rpx;
	}

	.progress-bar {
		flex: 1;
		height: 8rpx;
		background-color: #f0f0f0;
		border-radius: 4rpx;
		overflow: hidden;
	}

	.progress-fill {
		height: 100%;
		border-radius: 4rpx;
		background-color: #5b8ef3;
		transition: width 0.3s ease;
	}

	.fill-completed {
		background-color: #a8e6cf;
	}

	.fill-expired {
		background-color: #ff6b6b;
	}

	.goal-description {
		display: block;
		margin-top: 10rpx;
		font-size: 14px;
		color: #666;
		line-height: 1.4;
	}

	.children-container {
		margin-left: 40rpx;
		border-left: 1px dashed #eee;
		padding-left: 20rpx;
		overflow: hidden;
		transition: all 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
	}

	.children-wrapper {
		transition: all 0.3s ease;
	}

	.add-child-btn {
		display: flex;
		align-items: center;
		padding: 12rpx 16rpx;
		color: #999;
		font-size: 14px;
		background-color: #f9f9f9;
		border-radius: 8rpx;
		margin-top: 8rpx;
	}

	/* goal-node.vue */
	.children-container {
		margin-left: calc(20px + var(--depth, 0) * 15px);
		border-left: 1px solid #eee;
		padding-left: 12px;
	}

	.goal-card {
		border-left-width: 4px;
		border-left-color: hsl(calc(200 + var(--depth, 0) * 40), 80%, 60%);
	}
</style>