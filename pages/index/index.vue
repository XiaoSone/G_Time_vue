<template>
	<view class="page">
		<view class="container">
			<!-- 顶部信息区 -->
			<view class="header">
				<view class="header-left">
					<view class="greeting">下午好，{{userInfo.name}}</view>
					<view class="date">{{time}}</view>
					<view class="task-info">今日待办: {{ todoCount }}项 已完成: {{ completedTodoCount }}项</view>
				</view>
				<view class="progress-circle">
					<view class="progress-text">{{ progressPercentage }}%</view>
					<view class="progress-label">完成</view>
				</view>
			</view>

			<!-- 习惯追踪模块 -->
			<view class="habits-section">
				<!-- 头部 -->
				<view class="section-header">
					<text class="section-title">今日习惯</text>
					<text class="more-link cursor-pointer" @click="showHabitDetailModal = true">查看详情</text>
				</view>

				<!-- 习惯列表 -->
				<view class="habits-list" v-if="filteredHabits.length > 0">
					<view class="habit-card cursor-pointer" v-for="habit in filteredHabits" :key="habit.id">
						<text class="habit-name"
							:style="{color : habit.select ? '#a8e6cf' : '#000000'}">{{ habit.name }}</text>
						<text class="habit-streak">连续 {{ habit.streak }} 天</text>
						<view class="checkbox" :class="{ 'checked': habit.select }" @click="toggleHabit(habit.id)">
							<uni-icons v-if="habit.status" type="checkmarkempty" size="16" color="#fff" />
						</view>
					</view>
				</view>
				<!-- 没有习惯时的提示 -->
				<view class="empty-tip" v-else>
					<text>恭喜你！完成今日所有习惯打卡！！！</text>
				</view>
			</view>

			<!-- 弹出层：今日习惯详情 -->
			<view class="modal-mask" v-if="showHabitDetailModal" @click="showHabitDetailModal = false">
				<view class="modal-content" @click.stop>
					<view class="modal-header">
						<text class="modal-title">今日习惯详情</text>
						<uni-icons type="close" size="20" color="#999" @click="showHabitDetailModal = false" />
					</view>
					<view class="modal-body">
						<view class="habit-card" v-for="habit in habits" :key="habit.id">
							<text class="habit-name">{{ habit.name }}</text>
							<text class="habit-description">{{ habit.description }}</text>
							<text class="habit-streak">连续 {{ habit.streak }} 天</text>

							<view class="checkbox" :class="{ 'checked': habit.select }"
								@click="toggleHabitInModal(habit.id)">
								<uni-icons v-if="habit.status" type="checkmarkempty" size="16" color="#fff" />
							</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 待办事项列表 -->
			<view class="todos-section">
				<view class="section-header">
					<view class="title-group">
						<text class="section-title">待办事项</text>
						<view class="priority-indicators">
							<view class="priority-dot priority-high"></view>
							<view class="priority-dot priority-medium"></view>
							<view class="priority-dot priority-low"></view>
						</view>
					</view>
					<text class="todo-count">{{ todoCount }}项</text>
				</view>
				<view class="todos-list">
					<view class="todo-item" v-for="todo in sortedTodos()" :key="todo.id">
						<view class="todo-main">
							<view class="todo-checkbox" :class="{ 'checked': todo.status }"
								@click="toggleTodo(todo.id)">
								<uni-icons v-if="todo.status" type="checkmarkempty" size="16" color="#fff" />
							</view>
							<view class="todo-content">
								<view class="todo-title-row">
									<text class="todo-title"
										:class="{ 'completed': todo.status }">{{ todo.title }}</text>
									<view :class="['priority-dot', `priority-${getPriorityClass(todo.priority)}`]">
									</view>
								</view>
								<text class="todo-desc"
									:class="{ 'completed': todo.status }">{{ todo.description }}</text>
								<text class="todo-time"
									:class="{ 'completed': todo.status }">{{ `至 ` + todo.endTime }}</text>
							</view>
							<!-- <uni-icons class="more-icon cursor-pointer" type="more" size="20" color="#999" /> -->
						</view>
					</view>
				</view>
			</view>

			<!-- 目标进度 -->
			<view class="goals-section">
				<view class="section-header">
					<text class="section-title">目标进度</text>
					<!-- <text class="more-link cursor-pointer">查看详情</text> -->
				</view>
				<view class="goals-list">
					<view class="goal-item" v-for="goal in filteredGoals" :key="goal.id"
						@click="showGoalDetail(goal.id)">
						<view class="goal-header">
							<text class="goal-name">{{ goal.name }}</text>
							<text class="goal-percentage">{{ goal.progress }}%</text>
						</view>
						<text class="goal-description">{{ goal.description }}</text>
						<view class="progress-bar">
							<view class="progress-fill" :style="{ width: `${goal.progress}%` }"></view>
						</view>
					</view>
				</view>
			</view>

			<!-- 弹出层：目标详情 -->
			<view class="modal-mask" v-if="showGoalDetailModal" @click="showGoalDetailModal = false">
				<view class="modal-content" @click.stop>
					<view class="modal-header">
						<text class="modal-title">目标详情</text>
						<uni-icons type="close" size="20" color="#999" @click="showGoalDetailModal = false" />
					</view>
					<view class="modal-body">
						<view class="goal-detail" v-for="goalDetail in goalDetails" :key="goalDetail.id">
							<text class="goal-stage">阶段 {{ goalDetail.sequence }}: {{ goalDetail.name }}</text>
							<text class="goal-description">阶段详情：{{ goalDetail.description }}</text>
							<text class="goal-due-date">截止日期: {{ goalDetail.dueDate }}</text>
							<view class="goal-status">
								<text>状态: {{ goalDetail.status ? '已完成' : '未完成' }}</text>
								<view class="GoalDetailModal-checkbox" :class="{ 'checked': goalDetail.status }"
									@click="toggleStage(goalDetail)">
									<uni-icons v-if="goalDetail.status" type="checkmarkempty" size="16" color="#fff" />
								</view>
							</view>

						</view>
						<view class="empty-tip" v-if="goalDetails.length === 0">
							<text>还没有阶段目标哦！</text>
						</view>
					</view>
				</view>
			</view>



		</view>
	</view>
</template>

<script>
	import axios from 'axios';
	import dayjs from 'dayjs';
	import isBetween from 'dayjs/plugin/isBetween';

	import {
		ref
	} from 'vue';

	export default {
		data() {
			return {
				userInfo: [],
				habits: [],
				todos: [],
				goals: [],
				showHabitDetailModal: false,
				showGoalDetailModal: false,
				goalDetails: [],
				selectedGoalId: null,
				time: "",
			};
		},
		watch: {
			showHabitDetailModal(b) {
				this.fetchData();
			},
			showGoalDetailModal(b) {
				this.fetchData();
			},
		},
		computed: {
			todoCount() {
				return this.todos.length;
			},
			completedTodoCount() {
				return this.todos.filter(todo => todo.status).length;
			},
			progressPercentage() {
				const total = this.todos.length;
				const completed = this.todos.filter(todo => todo.status).length;
				return total === 0 ? 0 : Math.round((completed / total) * 100);
			},

			filteredHabits() {
				return this.habits.filter(habit => !habit.select);
			},
			filteredGoals() {
				return this.goals.filter(goal => {
					const currentDate = dayjs().format('YYYY-MM-DD');
					// 如果 startDate 或 dueDate 为空，则跳过
					if (!goal.startDate || !goal.dueDate) return false;

					const startDate = dayjs(goal.startDate);
					const dueDate = dayjs(goal.dueDate);

					// 判断当前日期是否在 startDate 和 dueDate 之间（包括边界）
					return dayjs(currentDate).isBetween(startDate, dueDate, 'day', '[]');
				});
			},
		},
		methods: {
			getPriorityClass(priority) {
				switch (priority) {
					case 3:
						return 'high';
					case 2:
						return 'medium';
					case 1:
						return 'low';
					default:
						return 'low';
				}
			},
			toggleHabit(id) {
				const habit = this.habits.find(h => h.id === id);
				if (habit) {
					// 如果未选中，则打卡
					if (!habit.select) {
						this.checkHabit(id);
					}
					// 更新选择框状态
					habit.select = !habit.select;
				}
			},
			toggleHabitInModal(id) {
				const habit = this.habits.find(habit => habit.id === id);
				if (habit) {
					habit.select = !habit.select;
					// 如果取消选中，删除今日的打卡记录
					if (!habit.select) {
						this.upHabitLog(id, habit.select);
						habit.streak -= 1;
					} else {
						this.checkHabit(id);

					}
				}
			},
			toggleTodo(id) {
				const todo = this.todos.find(t => t.id === id);
				if (todo) {
					todo.status = !todo.status;
					todo.status = todo.status ? 1 : 0;
					this.upTask(id);
				}
			},
			sortedTodos() {
				const todosCopy = JSON.parse(JSON.stringify(this.todos));
				return todosCopy.sort((a, b) => {
					if (a.status !== b.status) {
						return a.status ? 1 : -1;
					}
					return Number(b.priority) - Number(a.priority);
				});
			},
			async showGoalDetail(goalId) {
				this.selectedGoalId = goalId;
				const goal = this.goals.find(g => g.id === goalId);
				if (goal) {
					this.goalDetails = goal.stages.sort((a, b) => a.sequence - b.sequence);
					this.showGoalDetailModal = true;
				}
			},
			async toggleStage(stage) {
				const previousStage = this.goalDetails.find(s => s.sequence === stage.sequence - 1);
				if (previousStage && !previousStage.status) {
					uni.showToast({
						title: `请先完成阶段 ${previousStage.sequence}`,
						icon: 'none'
					});
					return;
				}
				stage.status = !stage.status;

				try {
					const token = uni.getStorageSync('token');
					const headers = {
						satoken: token
					};
					const updateStageStatusResponse = await axios.post(
						`/goals/${stage.goalId}/stages/${stage.id}/status`, {
							status: stage.status
						}, {
							headers
						});
				} catch (error) {
					console.error('Error fetching data:', error);
				}

			},
			async fetchData() {
				try {
					const token = uni.getStorageSync('token'); // 获取本地存储的 token
					// 将 token 添加到请求头
					const headers = {
						satoken: token
					};
					// 获取用户信息
					this.userInfo = uni.getStorageSync('user');

					// // 获取习惯列表
					// const habitsResponse = await axios.post('/habits/allHabits', {}, {
					// 	headers
					// });

					// // 获取今日的打卡记录
					// const today = dayjs().format('YYYY-MM-DD'); // 获取今天的日期（YYYY-MM-DD）
					// const todayLogsResponse = await axios.post('/habits/logs', {
					// 	date: today
					// }, {
					// 	headers
					// });
					// const todayLogs = todayLogsResponse.data.data; // 获取今日的打卡记录

					// // 初始化 habits，设置 select 属性
					// this.habits = habitsResponse.data.data.map(habit => ({
					// 	...habit,
					// 	select: todayLogs.some(log => log.habitId === habit.id && log
					// 		.isCompleted), // 根据打卡记录设置 select
					// }));
					// 获取当前日期信息
					const today = dayjs();
					const currentWeekStart = today.startOf('week').format('YYYY-MM-DD');
					const currentMonthStart = today.startOf('month').format('YYYY-MM-DD');

					// 并行请求所有必要数据
					const [habitsResponse, todayLogsResponse, weekLogsResponse, monthLogsResponse] = await Promise.all(
						[
							axios.post('/habits/allHabits', {}, {
								headers
							}),
							axios.post('/habits/logs', {
								date: today.format('YYYY-MM-DD')
							}, {
								headers
							}),
							axios.post('/habits/logs/range', {
								startDate: currentWeekStart,
								endDate: today.format('YYYY-MM-DD')
							}, {
								headers
							}),
							axios.post('/habits/logs/range', {
								startDate: currentMonthStart,
								endDate: today.format('YYYY-MM-DD')
							}, {
								headers
							})
						]);

					// 提取响应数据
					const habits = habitsResponse.data.data;
					const todayLogs = todayLogsResponse.data.data;
					const weekLogs = weekLogsResponse.data.data;
					const monthLogs = monthLogsResponse.data.data;

					// 处理习惯数据，根据频率设置select属性
					this.habits = habits.map(habit => {
						let select;

						switch (habit.frequency) {
							case 1: // 每日习惯
								select = todayLogs.some(log => log.habitId === habit.id && log.isCompleted);
								break;
							case 2: // 每周习惯
								// 只要本周内完成过就设为true
								select = weekLogs.some(log => log.habitId === habit.id && log.isCompleted);
								break;
							case 3: // 每月习惯
								// 只要本月内完成过就设为true
								select = monthLogs.some(log => log.habitId === habit.id && log.isCompleted);
								break;
							default:
								select = false; // 未知频率，默认设为false
						}

						return {
							...habit,
							select
						};
					});


					// 获取待办事项列表
					const nowTime = dayjs().format('YYYY-MM-DDTHH:mm:ss');
					const todosResponse = await axios.post('/task/nowTask', {
						date: nowTime
					}, {
						headers
					});
					this.todos = todosResponse.data.data.map(todo => ({
						...todo,
						completed: todo.status === 1,
					}));


					// 获取目标列表
					const goalsResponse = await axios.post('/goals/list', {}, {
						headers
					});

					this.goals = goalsResponse.data.data.map(goal => ({
						...goal,
						progress: goal.progress,
					}));

				} catch (error) {
					console.error('Error fetching data:', error);
				}
			},
			async checkHabit(id) {
				try {
					const token = uni.getStorageSync('token'); // 获取本地存储的 token
					const headers = {
						satoken: token
					}; // 将 token 添加到请求头

					// 调用打卡接口
					const response = await axios.post(`/habits/check/${id}`, {}, {
						headers
					});

					// 处理成功逻辑
					console.log('打卡成功:', response.data.message);

					// 更新 habits 列表中的 select 属性
					const habit = this.habits.find(h => h.id === id);
					if (habit) {
						habit.streak += 1;
						habit.select = true; // 更新选择框状态
					}
				} catch (error) {
					// 处理失败逻辑
					console.error('打卡失败:', error.response?.data?.message || error.message);
				}
			},
			async upHabitLog(id, isCompleted) {
				try {
					const token = uni.getStorageSync('token');
					const headers = {
						satoken: token
					};
					const today = new Date().toLocaleDateString('en-CA');
					const res = await axios.post('/habits/logs/updata', {
						date: today,
						habitId: id,
						isCompleted: isCompleted
					}, {
						headers
					});
					console.log('更新成功:', response.data.data);
				} catch (error) {
					console.error('更新失败:', error.response?.data?.message || error.message);
				}
			},
			async upTask(id) {
				try {
					const token = uni.getStorageSync('token');
					const headers = {
						satoken: token
					};
					const taskDTO = JSON.stringify(this.todos.find(s => s.id === id), null, 2);
					console.log(taskDTO);


					const res = await axios.post(`/task/update/${id}`, taskDTO, {
						headers
					});
					console.log('更新成功：', res.data.data);
				} catch (err) {
					console.log('更新失败：', err.response?.data?.message || err.message);
				}
			}
		},
		mounted() {
			this.fetchData();
		},
		created() {
			dayjs.extend(isBetween);
			const day = dayjs().day();

			// 根据星期几的值来显示对应的中文星期名称
			let weekDay;
			switch (day) {
				case 0:
					weekDay = '星期天';
					break;
				case 1:
					weekDay = '星期一';
					break;
				case 2:
					weekDay = '星期二';
					break;
				case 3:
					weekDay = '星期三';
					break;
				case 4:
					weekDay = '星期四';
					break;
				case 5:
					weekDay = '星期五';
					break;
				case 6:
					weekDay = '星期六';
					break;
				default:
					weekDay = '未知';
			}
			this.time = dayjs().format(`YYYY年MM月DD日，${weekDay}`);
		}
	};
</script>

<style>
	.page {
		height: 100%;
	}

	.container {
		padding: 32rpx;
	}

	.header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		margin-bottom: 40rpx;
	}

	.header-left {
		flex: 1;
	}

	.greeting {
		font-size: 36rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 8rpx;
	}

	.date {
		font-size: 24rpx;
		color: #999;
		margin-bottom: 16rpx;
		padding: 0;
	}

	.task-info {
		font-size: 24rpx;
		color: #666;
	}

	.progress-circle {
		width: 120rpx;
		height: 120rpx;
		border-radius: 60rpx;
		background-color: #a8e6cf;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		flex-shrink: 0;
	}

	.progress-text {
		font-size: 32rpx;
		color: #fff;
		font-weight: bold;
	}

	.progress-label {
		font-size: 24rpx;
		color: #fff;
	}

	.section-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 24rpx;
	}

	.title-group {
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.section-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
	}

	.more-link {
		font-size: 24rpx;
		color: #a8e6cf;
	}

	.habits-section {
		background-color: #fff;
		border-radius: 16rpx;
		padding: 24rpx;
		margin-bottom: 24rpx;
	}

	.habits-list {
		display: flex;
		gap: 24rpx;
		overflow-x: auto;
		scrollbar-width: none;
	}

	.habit-card {
		flex: 0 0 auto;
		background-color: #f8f9fa;
		border-radius: 12rpx;
		padding: 24rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		position: relative;
		width: calc(33.33%-20px);
		/* margin-right: 10px; */
	}

	.habit-name {
		font-size: 28rpx;
		color: #333;
		margin: 16rpx 0 8rpx;
	}

	.habit-description {
		font-size: 24rpx;
		color: #3b3b3b;
	}

	.habit-streak {
		font-size: 24rpx;
		color: #999;
	}


	.modal-mask {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.5);
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.modal-content {
		background-color: #fff;
		width: 80%;
		max-width: 400px;
		border-radius: 8px;
		padding: 16px;
	}

	.modal-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 16px;
	}

	.modal-title {
		font-size: 18px;
		font-weight: bold;
	}

	.modal-body {
		display: flex;
		flex-direction: column;
		gap: 12px;
	}

	.goal-detail {
		padding: 16rpx;
		border-bottom: 1px solid #f0f0f0;
	}

	.goal-stage,
	.goal-description,
	.goal-due-date,
	.goal-status {
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
		display: block;
	}

	.goal-stage {
		font-size: 28rpx;
		color: #333;
		margin-bottom: 8rpx;
	}

	.goal-description {
		font-size: 24rpx;
		color: #666;
		margin-bottom: 8rpx;
	}

	.goal-due-date {
		font-size: 24rpx;
		color: #999;
		margin-bottom: 8rpx;
	}

	.goal-status {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.GoalDetailModal-checkbox {
		width: 30rpx;
		height: 30rpx;
		border-radius: 20rpx;
		border: 2px solid #ddd;
		position: absolute;
		right: 80rpx;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.GoalDetailModal-checkbox.checked {
		background-color: #a8e6cf;
		border-color: #a8e6cf;
	}


	.checkbox {
		width: 30rpx;
		height: 30rpx;
		border-radius: 20rpx;
		border: 2px solid #ddd;
		position: absolute;
		top: 10rpx;
		right: 5rpx;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.checkbox.checked {
		background-color: #a8e6cf;
		border-color: #a8e6cf;
	}

	.todos-section {
		background-color: #fff;
		border-radius: 16rpx;
		padding: 24rpx;
		margin-bottom: 24rpx;
	}

	.priority-indicators {
		display: flex;
		align-items: center;
	}

	.priority-dot {
		border-radius: 50%;
		margin-right: 5px;
	}

	.priority-high {
		width: 16rpx;
		height: 16rpx;
		background-color: #ff7875;
	}

	.priority-medium {
		width: 12rpx;
		height: 12rpx;
		background-color: #ffc069;

	}

	.priority-low {
		width: 8rpx;
		height: 8rpx;
		background-color: #91d5ff;
	}

	.todo-count {
		font-size: 24rpx;
		color: #999;
	}

	.todo-item {
		padding: 24rpx 0;
		border-bottom: 1px solid #f0f0f0;
	}

	.todo-item:last-child {
		border-bottom: none;
	}

	.todo-main {
		display: flex;
		align-items: flex-start;
	}

	.todo-checkbox {
		width: 40rpx;
		height: 40rpx;
		border-radius: 8rpx;
		border: 2px solid #ddd;
		display: flex;
		justify-content: center;
		align-items: center;
		margin-right: 16rpx;
		flex-shrink: 0;
	}

	.todo-checkbox.checked {
		background-color: #a8e6cf;
		border-color: #a8e6cf;
	}

	.todo-content {
		flex: 1;
		margin-right: 24rpx;
	}

	.todo-title-row {
		display: flex;
		align-items: center;
		margin-bottom: 8rpx;
	}

	.todo-title {
		font-size: 28rpx;
		color: #333;
		margin-right: 8rpx;
	}

	.todo-title.completed {
		color: #999;
		text-decoration: line-through;
	}

	.todo-desc {
		font-size: 24rpx;
		color: #999;
		display: -webkit-box;
		margin-bottom: 8rpx;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 1;
		overflow: hidden;
	}

	.todo-desc.completed {
		color: #ccc;
		text-decoration: line-through;
	}

	.todo-time {
		font-size: 24rpx;
		color: #666;
		display: block;
	}

	.todo-time.completed {
		color: #999;
		text-decoration: line-through;
	}

	.goals-section {
		background-color: #fff;
		border-radius: 16rpx;
		padding: 24rpx;
	}

	.goal-item {
		margin-bottom: 80rpx;
	}

	.goal-header {
		display: flex;
		justify-content: space-between;
		margin-bottom: 12rpx;
	}

	.goal-name {
		font-size: 28rpx;
		color: #333;
	}

	.goal-percentage {
		font-size: 28rpx;
		color: #999;
	}

	.progress-bar {
		height: 16rpx;
		background-color: #f0f0f0;
		border-radius: 8rpx;
		overflow: hidden;
	}

	.progress-fill {
		height: 100%;
		background-color: #a8e6cf;
		border-radius: 8rpx;
		transition: width 0.3s ease;
	}

	.cursor-pointer {
		cursor: pointer;
	}

	.empty-tip {
		display: block;
		width: 100%;
		height: 20px;
		position: relative;
	}
</style>