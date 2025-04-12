<template>
	<view class="habit-tracker">
		<!-- 顶部标签栏 -->
		<view class="tab-container">
			<view v-for="(tab, index) in tabs" :key="index" class="tab-item" :class="{ active: currentTab === index }"
				@click="switchTab(index)">
				<text class="tab-text">{{ tab }}</text>
			</view>
			<view class="tab-indicator" :style="{ left: indicatorLeft + 'rpx' }"></view>
		</view>

		<!-- 内容区域 -->
		<swiper class="content-swiper" :current="currentTab" @change="handleSwiperChange">
			<!-- 日历视图 -->
			<swiper-item>
				<view class="calendar-view">
					<!-- 这里原来放置日历内容，现在为空 -->
					<uni-calendar :insert="true" :lunar="true" @change="handleCalendarDateChange" />
				</view>
			</swiper-item>

			<!-- 其他视图... -->
			<!-- 列表视图 -->
			<swiper-item>
				<scroll-view class="list-view" scroll-y>
					<!-- 添加日期显示容器 -->
					<view class="date-header">
						<text class="date-text">{{ displayDate }}</text>
						<text class="weekday-text">{{ weekday }}</text>
					</view>
					<view class="habit-list">
						<view v-for="(habit, index) in habits" :key="index" class="habit-card"
							:class="{ 'completed-today': habit.completedToday }" @longpress="showHabitActions(habit)"
							@touchend.prevent="handleLongPressEnd" @touchcancel.prevent="handleLongPressEnd">
							<view class="habit-header">
								<text class="habit-name">{{ habit.name }}</text>
								<view class="habit-category-frequency">
									<view class="habit-category"
										:style="{ backgroundColor: getCategoryColor(habit.category) }">
										<text class="category-text">{{ habit.category }}</text>
									</view>
									<view class="frequency-badge">
										<text class="frequency-text">{{ getFrequencyLabel(habit.frequency) }}</text>
									</view>
								</view>
							</view>
							<view class="habit-streak">
								<text class="streak-text">连续：{{ habit.currentStreak }} 天</text>
								<text class="streak-best">最佳：{{ habit.bestStreak }} 天</text>
							</view>
							<view class="habit-progress">
								<view class="progress-bar">
									<view class="progress-fill" :style="{ width: habit.completionRate + '%' }"></view>
								</view>
								<text class="progress-text">{{ habit.completionRate }}%</text>
							</view>
							<view v-if="habit.completedToday" class="completed-badge">
								<uni-icons type="checkbox-filled" size="16" color="#ffffff"></uni-icons>
								<text class="completed-text">今日已完成</text>
							</view>
						</view>
					</view>
					<!-- 列表空状态 -->
					<view v-if="habits.length === 0" class="empty-state">
						<text class="empty-text">还没有添加习惯</text>
						<view class="add-habit-btn cursor-pointer" @click="showAddHabitModal">
							<text class="add-habit-text">添加新习惯</text>
						</view>
					</view>
				</scroll-view>

				<!-- 习惯操作菜单 -->
				<view v-if="habitActionsVisible" class="habit-actions-overlay" @touchmove.prevent.stop
					@click="hideHabitActions">
					<view class="habit-actions-modal" @click.stop @touchstart.stop @touchmove.stop @touchend.stop>
						<text class="habit-actions-title">{{ selectedHabit?.name }}</text>
						<view class="habit-actions-buttons">
							<view class="habit-action-item cursor-pointer" @click="toggleHabitCompletion">
								<uni-icons :type="selectedHabit?.completedToday ? 'close' : 'checkbox-filled'" size="20"
									:color="selectedHabit?.completedToday ? '#FF5252' : '#4CAF50'"></uni-icons>
								<text
									class="habit-action-text">{{ selectedHabit?.completedToday ? '取消完成' : '标记完成' }}</text>
							</view>
							<view class="habit-action-item cursor-pointer" @click="editHabit">
								<uni-icons type="compose" size="20" color="#5b8ef3"></uni-icons>
								<text class="habit-action-text">编辑习惯</text>
							</view>
							<view class="habit-action-item cursor-pointer" @click="confirmDeleteHabit">
								<uni-icons type="trash" size="20" color="#FF5252"></uni-icons>
								<text class="habit-action-text">删除习惯</text>
							</view>
						</view>
					</view>
				</view>

				<!-- 编辑习惯模态框 -->
				<view v-if="editHabitModalVisible" class="edit-modal-overlay" @click="hideEditHabitModal">
					<view class="edit-modal-container" @click.stop>
						<text class="modal-title">{{ editModalTitle }}</text>

						<view class="form-item">
							<text class="form-label">习惯名称</text>
							<input class="form-input" v-model="editHabitForm.name" placeholder="请输入习惯名称" />
						</view>

						<view class="form-item">
							<text class="form-label">习惯描述</text>
							<textarea class="form-textarea" v-model="editHabitForm.description"
								placeholder="请输入习惯描述(可选)" />
						</view>

						<view class="form-item">
							<text class="form-label">频率</text>
							<picker class="form-picker" mode="selector" :range="frequencyOptions" range-key="label"
								@change="handleFrequencyChange">
								<view class="picker-view">
									<text>{{ selectedFrequencyLabel || '请选择频率' }}</text>
									<uni-icons type="arrowdown" size="16" color="#999999"></uni-icons>
								</view>
							</picker>
						</view>

						<view class="form-item">
							<text class="form-label">分类</text>
							<picker class="form-picker" mode="selector" :range="categories" range-key="label"
								@change="handleCategoryChange">
								<view class="picker-view">
									<text>{{ editHabitForm.category || '请选择分类' }}</text>
									<uni-icons type="arrowdown" size="16" color="#999999"></uni-icons>
								</view>
							</picker>
						</view>

						<view class="form-row">
							<view class="form-item date-item">
								<text class="form-label">开始日期</text>
								<uni-datetime-picker class="datetime-picker" type="date"
									v-model="editHabitForm.startDate" :clearIcon="false" />
							</view>

							<view class="form-item date-item">
								<text class="form-label">结束日期</text>
								<uni-datetime-picker class="datetime-picker" type="date" v-model="editHabitForm.endDate"
									:start="editHabitForm.startDate" :clearIcon="false" />
							</view>
						</view>

						<!-- 新增状态选择 -->
						<view class="form-item">
							<text class="form-label">状态</text>
							<radio-group class="status-radio-group" @change="handleStatusChange">
								<label class="status-radio-item" v-for="(status, index) in statusOptions" :key="index">
									<radio :value="status.value" :checked="editHabitForm.status === status.value"
										:color="status.color" />
									<text>{{ status.label }}</text>
								</label>
							</radio-group>
						</view>

						<view class="modal-actions">
							<view class="modal-btn cancel-btn" @click="hideEditHabitModal">取消</view>
							<view class="modal-btn confirm-btn" @click="submitHabitUpdate">保存</view>
						</view>
					</view>
				</view>


				<!-- 添加习惯模态框 -->
				<view v-if="addHabitModalVisible" class="edit-modal-overlay" @click="hideAddHabitModal">
					<view class="edit-modal-container" @click.stop>
						<text class="modal-title">添加新习惯</text>

						<view class="form-item">
							<text class="form-label">习惯名称</text>
							<input class="form-input" v-model="addHabitForm.name" placeholder="请输入习惯名称(必填)" />
						</view>

						<view class="form-item">
							<text class="form-label">习惯描述</text>
							<textarea class="form-textarea" v-model="addHabitForm.description"
								placeholder="请输入习惯描述(可选)" />
						</view>

						<view class="form-item">
							<text class="form-label">频率</text>
							<picker class="form-picker" mode="selector" :range="frequencyOptions" range-key="label"
								@change="handleAddFrequencyChange">
								<view class="picker-view">
									<text>{{ addHabitForm.frequency ? frequencyOptions.find(f => f.value === addHabitForm.frequency)?.label : '请选择频率(必选)' }}</text>
									<uni-icons type="arrowdown" size="16" color="#999999"></uni-icons>
								</view>
							</picker>
						</view>

						<view class="form-item">
							<text class="form-label">分类</text>
							<picker class="form-picker" mode="selector" :range="categories" range-key="label"
								@change="handleAddCategoryChange">
								<view class="picker-view">
									<text>{{ addHabitForm.category || '请选择分类(必选)' }}</text>
									<uni-icons type="arrowdown" size="16" color="#999999"></uni-icons>
								</view>
							</picker>
						</view>

						<view class="form-row">
							<view class="form-item date-item">
								<text class="form-label">开始日期</text>
								<uni-datetime-picker class="datetime-picker" type="date"
									v-model="addHabitForm.startDate" :clearIcon="false" />
							</view>

							<view class="form-item date-item">
								<text class="form-label">结束日期</text>
								<uni-datetime-picker class="datetime-picker" type="date" v-model="addHabitForm.endDate"
									:start="addHabitForm.startDate" :clearIcon="false" />
							</view>
						</view>

						<view class="modal-actions">
							<view class="modal-btn cancel-btn" @click="hideAddHabitModal">取消</view>
							<view class="modal-btn confirm-btn" @click="submitAddHabit">添加</view>
						</view>
					</view>
				</view>

				<!-- 删除习惯确认模态框 -->
				<view v-if="confirmDeleteHabitVisible" class="confirm-delete-overlay" @click="cancelDeleteHabit">
					<view class="confirm-delete-modal" @click.stop>
						<text class="confirm-delete-title">确认删除习惯</text>
						<text
							class="confirm-delete-message">确定要删除"{{ selectedHabit?.name }}"习惯吗？此操作将删除所有相关记录且不可撤销。</text>
						<view class="confirm-delete-buttons">
							<view class="confirm-btn cancel-btn cursor-pointer" @click="cancelDeleteHabit">
								<text>取消</text>
							</view>
							<view class="confirm-btn delete-confirm-btn cursor-pointer" @click="deleteHabit">
								<text>删除</text>
							</view>
						</view>
					</view>
				</view>

				<!-- 悬浮添加按钮 -->
				<view class="floating-btn cursor-pointer" @click="showAddHabitModal">
					<uni-icons type="plus" size="24" color="#ffffff"></uni-icons>
				</view>
			</swiper-item>

			<!-- 统计视图 -->
			<swiper-item>
				<scroll-view class="stats-view" scroll-y>
					<view class="stats-container">
						<view class="stats-header">
							<text class="stats-title">习惯统计</text>
							<view class="time-selector cursor-pointer"
								@click.stop="showTimeSelector = !showTimeSelector">
								<text class="time-text">{{ selectedTimeRange }}</text>
								<uni-icons :type="showTimeSelector ? 'up' : 'down'" size="14" color="#666"></uni-icons>

								<view v-if="showTimeSelector" class="time-dropdown">
									<view v-for="(range, index) in timeRanges" :key="index"
										class="time-option cursor-pointer"
										:class="{ active: selectedTimeRange === range }"
										@click.stop="selectTimeRange(range)">
										<text>{{ range }}</text>
									</view>
								</view>
							</view>
						</view>

						<!-- 数据分析卡片 -->
						<view class="stats-summary">
							<view class="stats-card" :style="{ backgroundColor: getCategoryColor('健康') }">
								<text class="stats-value">{{ statsData.totalHabits }}</text>
								<text class="stats-label">总习惯数</text>
							</view>
							<view class="stats-card" :style="{ backgroundColor: getCategoryColor('学习') }">
								<text class="stats-value">{{ statsData.completionRate }}%</text>
								<text class="stats-label">总体完成率</text>
							</view>
							<view class="stats-card" :style="{ backgroundColor: getCategoryColor('社交') }">
								<text class="stats-value">{{ statsData.currentStreak }}</text>
								<text class="stats-label">最长连续天数</text>
							</view>
						</view>

						<!-- 完成率趋势图 -->
						<view class="stats-chart">
							<view class="chart-header">
								<text class="chart-title">完成率趋势</text>
								<view class="chart-legend">
									<view class="legend-item">
										<view class="legend-color" style="background-color: #5b8ef3;"></view>
										<text class="legend-text">完成率</text>
									</view>
								</view>
							</view>
							<qiun-data-charts type="line" :chartData="completionRateChartData" :ontouch="true"
								:animation="true" :errorShow="false" :tooltipFormat="tooltipFormat"
								canvasId="completionRateChart" canvas2d />
						</view>

						<!-- 分类分布图 -->
						<view class="stats-chart">
							<view class="chart-header">
								<text class="chart-title">分类分布</text>
							</view>
							<qiun-data-charts type="pie" :chartData="categoryDistributionChartData"
								canvasId="categoryDistributionChart" :animation="true" canvas2d />
						</view>

						<!-- 习惯表现排行榜 -->
						<view class="habit-performance">
							<view class="performance-header">
								<text class="performance-title">习惯表现排名</text>
								<view class="performance-filter" @click="togglePerformanceFilter">
									<text>{{ performanceFilterLabel }}</text>
									<uni-icons type="arrowdown" size="14" color="#666"></uni-icons>
								</view>
							</view>

							<view v-for="(habit, index) in sortedHabits" :key="habit.id" class="performance-item">
								<view class="performance-rank" :style="{ color: performanceRankColor(index) }">
									{{ index + 1 }}
								</view>
								<view class="performance-info">
									<text class="performance-name">{{ habit.name }}</text>
									<view class="performance-meta">
										<text class="performance-category"
											:style="{ backgroundColor: getCategoryColor(habit.category) }">
											{{ habit.category }}
										</text>
										<text class="performance-streak">连续 {{ habit.currentStreak }} 天</text>
									</view>
								</view>
								<view class="performance-value">
									<text>{{ habit.completionRate }}%</text>
								</view>
							</view>
						</view>

						<!-- 空状态 -->
						<view v-if="habits.length === 0" class="empty-state">
							<image class="empty-image" src="/static/images/empty-stats.png" mode="aspectFit"></image>
							<text class="empty-text">添加习惯开始追踪统计</text>
							<view class="add-habit-btn cursor-pointer" @click="showAddHabitModal">
								<text class="add-habit-text">添加新习惯</text>
							</view>
						</view>
					</view>
				</scroll-view>
			</swiper-item>

		</swiper>
	</view>
</template>

<script lang="ts">
	import { defineComponent } from 'vue';
	import axios from 'axios';
	import dayjs from 'dayjs';
	import isBetween from "dayjs/plugin/isBetween";

	// 定义习惯接口类型
	interface Habit {
		id : number;
		name : string;
		description ?: string;
		frequency : number;
		category : string;
		currentStreak : number;
		bestStreak : number;
		completionRate : number;
		completedToday : boolean;
		startDate : string;
		endDate : string;
		status : number;
	};
	interface HabitEditForm {
		id : number;
		name : string;
		description : string;
		frequency : number;
		category : string;
		startDate : string;
		endDate : string;
		currentStreak : number;
		bestStreak : number;
		status : number;
	}
	interface AddHabitForm {
		name : string;
		description : string;
		frequency : number;
		category : string;
		startDate : string;
		endDate : string;
	}
	interface StatsData {
		totalHabits : number;
		completedCount : number;
		completionRate : number;
		currentStreak : number;
		bestStreak : number;
	}
	interface ChartData {
		categories : string[];
		series : {
			name : string;
			data : number[];
			color ?: string;
		}[];
	}
	interface ChartDataPie {
		categories : string[];
		series : {
			name : string;
			data : number;
			color ?: string;
		}[];
	}

	export default defineComponent({
		data() {
			const today = new Date().toISOString().split('T')[0];
			const defaultEndDate = dayjs(today).add(30, 'day').format('YYYY-MM-DD');

			return {
				// 标签页数据
				tabs: ['日历', '列表', '统计'],
				currentTab: 0,
				indicatorLeft: 0,
				habits: [] as Habit[],
				loading: false,
				error: null as string | null,

				// 习惯操作相关
				habitActionsVisible: false,
				selectedHabit: null as Habit | null,
				confirmDeleteHabitVisible: false,

				// 添加习惯相关
				addHabitModalVisible: false,
				addHabitForm: {
					name: '',
					description: '',
					frequency: 1, // 默认为每日
					category: '',
					startDate: today,
					endDate: defaultEndDate
				} as AddHabitForm,

				// 编辑习惯相关
				editHabitModalVisible: false,
				editHabitForm: {
					id: 0,
					name: '',
					description: '',
					category: '',
					startDate: '',
					endDate: '',
					currentStreak: 0,
					bestStreak: 0,
					status: 1
				} as HabitEditForm,

				// 选项相关
				frequencyOptions: [
					{ label: '每日', value: 1 },
					{ label: '每周', value: 2 },
					{ label: '每月', value: 3 }
				],
				categories: [
					{ label: '学习', value: '学习' },
					{ label: '健康', value: '健康' },
					{ label: '心理', value: '心理' },
					{ label: '工作', value: '工作' },
					{ label: '社交', value: '社交' }
				],
				statusOptions: [
					{ label: '活跃', value: 1, color: '#5b8ef3' },
					{ label: '停止', value: 0, color: '#FF5252' }
				],

				// 长按定时器
				longPressTimer: null as number | null,
				selectedDate: '',

				displayDate: '', // 显示格式化的日期
				weekday: '',


				// 
				selectedTimeRange: '近7天',
				timeRanges: ['今天', '近7天', '近30天', '全部'],
				showTimeSelector: false,
				performanceFilter: 'completionRate', // completionRate | streak
				statsData: {
					totalHabits: 0,
					completedCount: 0,
					completionRate: 0,
					currentStreak: 0,
					bestStreak: 0
				} as StatsData,
				completionRateChartData: {} as ChartData,
				categoryDistributionChartData: {} as ChartDataPie
				// habitsByCategoryData: {} as PieChartData

			};
		},
		computed: {
			// 计算选中的频率标签
			selectedFrequencyLabel() {
				const selected = this.frequencyOptions.find(opt => opt.value === this.editHabitForm.frequency);
				return selected ? selected.label : '';
			},
			// 模态框标题(编辑/添加)
			editModalTitle() {
				return this.editHabitForm.id ? '编辑习惯' : '添加习惯';
			},


			// 
			performanceFilterLabel() {
				return this.performanceFilter === 'completionRate' ? '按完成率' : '按连续天数';
			},
			sortedHabits() {
				const habits = [...this.habits];
				if (this.performanceFilter === 'completionRate') {
					return habits.sort((a, b) => b.completionRate - a.completionRate);
				} else {
					return habits.sort((a, b) => b.currentStreak - a.currentStreak);
				}
			}
		},
		watch: {
			habits: {
				immediate: true,
				handler(newHabits) {
					if (newHabits && newHabits.length > 0) {
						this.calculateStats();
						this.prepareChartData();
					}
				}
			},
			selectedTimeRange() {
				this.prepareChartData();
			}
		},
		methods: {
			// 标签切换相关方法
			switchTab(index : number) {
				this.currentTab = index;
				this.updateIndicatorPosition();

				// 当切换到列表视图时
				if (index === 1) {
					if (this.selectedDate) {
						this.fetchHabitsForDate(this.selectedDate);
					} else {
						this.showCurrentDayHabits();
					}
				}
			},

			handleSwiperChange(e : any) {
				this.currentTab = e.detail.current;
				this.updateIndicatorPosition();
			},
			updateIndicatorPosition() {
				this.indicatorLeft = this.currentTab * (750 / 3);
			},

			// 长按相关方法
			handleLongPressStart(habit : any) {
				if (this.longPressTimer) {
					clearTimeout(this.longPressTimer);
					this.longPressTimer = null;
				}
				this.longPressTimer = setTimeout(() => {
					this.showHabitActions(habit);
					this.longPressTimer = null;
				}, 600) as unknown as number;
			},
			handleLongPressEnd() {
				if (this.longPressTimer) {
					clearTimeout(this.longPressTimer);
					this.longPressTimer = null;
				}
			},

			// 分类和频率相关方法
			getCategoryColor(category : string) {
				const colors : Record<string, string> = {
					'学习': '#4FC3F7',
					'健康': '#81C784',
					'心理': '#9575CD',
					'工作': '#FFB74D',
					'社交': '#FF8A65'
				};
				return colors[category] || '#BDBDBD';
			},
			getFrequencyLabel(frequency : number) : string {
				switch (frequency) {
					case 1: return '每日';
					case 2: return '每周';
					case 3: return '每月';
					default: return '未知';
				}
			},

			// 习惯操作菜单相关方法
			showHabitActions(habit : Habit) {
				this.selectedHabit = habit;
				this.habitActionsVisible = true;
			},
			hideHabitActions() {
				this.habitActionsVisible = false;
				this.selectedHabit = null;
			},

			// 添加习惯相关方法
			showAddHabitModal() {
				const today = new Date().toISOString().split('T')[0];
				this.addHabitForm = {
					name: '',
					description: '',
					frequency: 1, // 默认每日
					category: '学习', // 默认学习分类
					startDate: today,
					endDate: dayjs(today).add(30, 'day').format('YYYY-MM-DD')
				};
				this.addHabitModalVisible = true;
			},
			hideAddHabitModal() {
				this.addHabitModalVisible = false;
			},
			handleAddCategoryChange(e : any) {
				const index = e.detail.value;
				this.addHabitForm.category = this.categories[index].value;
			},
			handleAddFrequencyChange(e : any) {
				const index = e.detail.value;
				this.addHabitForm.frequency = this.frequencyOptions[index].value;
			},
			async submitAddHabit() {
				if (!this.addHabitForm.name || !this.addHabitForm.category || !this.addHabitForm.frequency) {
					uni.showToast({
						title: '请填写名称、分类和频率',
						icon: 'none'
					});
					return;
				}

				try {
					const token = uni.getStorageSync('token');
					if (!token) {
						throw new Error('未找到token，请先登录');
					}

					const response = await axios.post('/habits/create', {
						name: this.addHabitForm.name,
						description: this.addHabitForm.description,
						category: this.addHabitForm.category,
						frequency: this.addHabitForm.frequency,
						startDate: this.addHabitForm.startDate,
						endDate: this.addHabitForm.endDate,
						status: 1 // 默认设为活跃状态
					}, {
						headers: { 'satoken': token }
					});

					uni.showToast({
						title: '习惯添加成功',
						icon: 'success'
					});

					this.hideAddHabitModal();
					this.fetchHabits();
				} catch (err) {
					console.error('添加习惯失败:', err);
					uni.showToast({
						title: '添加失败，请重试',
						icon: 'none'
					});
				}
			},

			// 编辑习惯相关方法
			editHabit() {
				if (!this.selectedHabit) return;
				this.showEditHabitModal(this.selectedHabit);
				this.hideHabitActions();
			},
			showEditHabitModal(habit : Habit) {
				const today = new Date().toISOString().split('T')[0];
				this.editHabitForm = {
					id: habit.id,
					name: habit.name,
					description: habit.description || '',
					category: habit.category,
					frequency: habit.frequency ?? 1,
					startDate: habit.startDate || today,
					endDate: habit.endDate || dayjs(today).add(30, 'day').format('YYYY-MM-DD'),
					currentStreak: habit.currentStreak,
					bestStreak: habit.bestStreak,
					status: (habit.status ? 1 : 0) ?? 1
				};
				this.editHabitModalVisible = true;
			},
			hideEditHabitModal() {
				this.editHabitModalVisible = false;
			},
			handleCategoryChange(e : any) {
				const index = e.detail.value;
				this.editHabitForm.category = this.categories[index].value;
			},
			handleFrequencyChange(e : any) {
				const index = e.detail.value;
				this.editHabitForm.frequency = this.frequencyOptions[index].value;
			},
			handleStatusChange(e : any) {
				this.editHabitForm.status = parseInt(e.detail.value);
			},

			// 习惯完成/取消相关方法
			async toggleHabitCompletion() {
				if (!this.selectedHabit) return;

				try {
					const token = uni.getStorageSync('token');
					const habitId = this.selectedHabit.id;
					const isCurrentlyCompleted = this.selectedHabit.completedToday;

					if (isCurrentlyCompleted) {
						const today = new Date().toLocaleDateString('en-CA');
						await axios.post('/habits/logs/updata', {
							date: today,
							habitId: habitId,
							isCompleted: 0
						}, {
							headers: { 'satoken': token }
						});
					} else {
						await axios.post(`/habits/check/${habitId}`, null, {
							headers: { 'satoken': token }
						});
					}

					this.selectedHabit.completedToday = !isCurrentlyCompleted;

					uni.showToast({
						title: this.selectedHabit.completedToday ? '已标记为完成' : '已取消完成',
						icon: 'success'
					});

					this.hideHabitActions();
					this.fetchHabits();
				} catch (err) {
					console.error('更新习惯状态失败:', err);
					uni.showToast({
						title: '操作失败，请重试',
						icon: 'none'
					});
					this.fetchHabits();
				}
			},

			// 删除习惯相关方法
			confirmDeleteHabit() {
				if (!this.selectedHabit) return;
				this.confirmDeleteHabitVisible = true;
				this.habitActionsVisible = false;
			},
			cancelDeleteHabit() {
				this.confirmDeleteHabitVisible = false;
			},
			async deleteHabit() {
				if (!this.selectedHabit) return;

				try {
					const token = uni.getStorageSync('token');
					const habitId = this.selectedHabit.id;

					await axios.post(`/habits/delete/${habitId}`, null, {
						headers: { 'satoken': token }
					});

					this.habits = this.habits.filter(h => h.id !== habitId);
					uni.showToast({
						title: '习惯已删除',
						icon: 'success'
					});

					this.confirmDeleteHabitVisible = false;
					this.selectedHabit = null;
					this.fetchHabits();
				} catch (err) {
					console.error('删除习惯失败:', err);
					uni.showToast({
						title: '删除失败，请重试',
						icon: 'none'
					});
				}
			},

			// 提交习惯更新
			async submitHabitUpdate() {
				if (!this.editHabitForm.name || !this.editHabitForm.category || !this.editHabitForm.frequency) {
					uni.showToast({
						title: '请填写名称、分类和频率',
						icon: 'none'
					});
					return;
				}

				try {
					const token = uni.getStorageSync('token');
					const response = await axios.post(`/habits/update/${this.editHabitForm.id}`, {
						name: this.editHabitForm.name,
						description: this.editHabitForm.description,
						category: this.editHabitForm.category,
						frequency: this.editHabitForm.frequency,
						startDate: this.editHabitForm.startDate,
						endDate: this.editHabitForm.endDate,
						streak: this.editHabitForm.currentStreak,
						maxStreak: this.editHabitForm.bestStreak,
						status: this.editHabitForm.status
					}, {
						headers: { 'satoken': token }
					});

					uni.showToast({
						title: '习惯更新成功',
						icon: 'success'
					});

					this.hideEditHabitModal();
					this.fetchHabits();
				} catch (err) {
					console.error('更新习惯失败:', err);
					uni.showToast({
						title: '更新失败，请重试',
						icon: 'none'
					});
				}
			},

			// 获取习惯列表
			async fetchHabits() {
				this.loading = true;
				this.error = null;

				try {
					const token = uni.getStorageSync('token');
					if (!token) {
						throw new Error('未找到token，请先登录');
					}

					const headers = { 'satoken': token };
					const today = dayjs();
					this.displayDate = today.format('YYYY年MM月DD日');
					const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
					this.weekday = weekdays[today.day()];
					const currentWeekStart = today.startOf('week').format('YYYY-MM-DD');
					const currentMonthStart = today.startOf('month').format('YYYY-MM-DD');

					const [habitsResponse, todayLogsResponse, weekLogsResponse, monthLogsResponse] = await Promise.all([
						axios.post('/habits/allHabits', {}, { headers }),
						axios.post('/habits/logs', { date: today.format('YYYY-MM-DD') }, { headers }),
						axios.post('/habits/logs/range', {
							startDate: currentWeekStart,
							endDate: today.format('YYYY-MM-DD')
						}, { headers }),
						axios.post('/habits/logs/range', {
							startDate: currentMonthStart,
							endDate: today.format('YYYY-MM-DD')
						}, { headers })
					]);

					const todayLogs = todayLogsResponse.data.data;
					const weekLogs = weekLogsResponse.data.data;
					const monthLogs = monthLogsResponse.data.data;

					this.habits = habitsResponse.data.data
						.map((item : any) => {
							const habit : Habit = {
								id: item.id,
								name: item.name,
								description: item.description,
								frequency: item.frequency || 1,
								category: item.category || '未分类',
								currentStreak: item.streak || 0,
								bestStreak: item.maxStreak || 0,
								completionRate: this.calculateCompletionRate(item),
								completedToday: false,
								startDate: item.startDate,
								endDate: item.endDate,
								status: item.status
							};

							switch (habit.frequency) {
								case 1:
									habit.completedToday = todayLogs.some((log : any) => log.habitId === habit.id && log.isCompleted === true);
									break;
								case 2:
									habit.completedToday = weekLogs.some((log : any) => log.habitId === habit.id && log.isCompleted === true);
									break;
								case 3:
									habit.completedToday = monthLogs.some((log : any) => log.habitId === habit.id && log.isCompleted === true);
									break;
							}

							return habit;
						}).filter(habit => today.isBetween(habit.startDate, habit.endDate, "day", "[]"))
				} catch (err : any) {
					this.error = err.message || '获取习惯列表失败';
					console.error('获取习惯列表错误:', err);
					uni.showToast({
						title: this.error,
						icon: 'none'
					});
				} finally {
					this.loading = false;
				}
			},

			// 计算完成率
			calculateCompletionRate(habit : any) : number {
				if (!habit.startDate || !habit.endDate) return 0;

				const startDate = dayjs(habit.startDate);
				const endDate = dayjs(habit.endDate);
				const today = dayjs().format('YYYY-MM-DD');

				if (dayjs(today).isBefore(startDate)) return 0;
				if (dayjs(today).isAfter(endDate)) return 100;

				const totalDays = dayjs(endDate).diff(startDate, 'day');
				const passedDays = Math.max(dayjs(today).diff(startDate, 'day'), 1);
				console.log(passedDays);
				return Math.round(Math.min((habit.streak / passedDays) * 100, 100));
			},

			handleCalendarDateChange(e : any) {
				// 存储选中的日期
				this.selectedDate = e.fulldate;
				// 切换到列表视图
				this.switchTab(1);
				// 加载该日期的习惯数据
				this.fetchHabitsForDate(e.fulldate);
			},
			async fetchHabitsForDate(date : string) {
				this.loading = true;
				this.error = null;

				// 更新显示日期
				this.updateDisplayDate(date);

				try {
					const token = uni.getStorageSync('token');
					if (!token) {
						throw new Error('未找到token，请先登录');
					}

					const [habitsResponse, dateLogsResponse] = await Promise.all([
						axios.post('/habits/allHabits', {}, { headers: { 'satoken': token } }),
						axios.post('/habits/logs', { date }, { headers: { 'satoken': token } })
					]);

					const dateLogs = dateLogsResponse.data.data;

					this.habits = habitsResponse.data.data
						.map((item : any) => {
							const habit : Habit = {
								id: item.id,
								name: item.name,
								description: item.description,
								frequency: item.frequency || 1,
								category: item.category || '未分类',
								currentStreak: item.streak || 0,
								bestStreak: item.maxStreak || 0,
								completionRate: this.calculateCompletionRate(item),
								completedToday: false,
								startDate: item.startDate,
								endDate: item.endDate,
								status: item.status
							};

							// 检查该习惯是否在选中日期有完成记录
							habit.completedToday = dateLogs.some((log : any) =>
								log.habitId === habit.id && log.isCompleted === true
							);

							return habit;
						});

					// 过滤出在选中日期有效的习惯
					const selectedDate = dayjs(date);
					this.habits = this.habits.filter(habit => {
						const startDate = dayjs(habit.startDate);
						const endDate = dayjs(habit.endDate);
						return selectedDate.isBetween(startDate, endDate, null, '[]');
					});

					// uni.showToast({
					// 	title: `显示${date}的习惯列表`,
					// 	icon: 'none'
					// });

				} catch (err : any) {
					console.error('获取日期习惯列表错误:', err);
					uni.showToast({
						title: '获取习惯列表失败',
						icon: 'none'
					});
				} finally {
					this.loading = false;
				}
			},
			showCurrentDayHabits() {
				const today = dayjs().format('YYYY-MM-DD');
				this.fetchHabitsForDate(today);
			},

			updateDisplayDate(dateString : string) {
				const date = dayjs(dateString);
				this.displayDate = date.format('YYYY年MM月DD日');

				// 处理星期几显示
				const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
				this.weekday = weekdays[date.day()];

				// 存储原始日期格式
				this.selectedDate = dateString;
			},


			// 
			selectTimeRange(range : string) {
				this.selectedTimeRange = range;
				this.showTimeSelector = false;
			},

			togglePerformanceFilter() {
				this.performanceFilter = this.performanceFilter === 'completionRate' ? 'streak' : 'completionRate';
			},

			calculateStats() {
				if (!this.habits || this.habits.length === 0) return;

				this.statsData.totalHabits = this.habits.length;
				this.statsData.completionRate = Math.round(
					this.habits.reduce((sum, habit) => sum + habit.completionRate, 0) / this.habits.length
				);
				this.statsData.currentStreak = Math.max(...this.habits.map(h => h.currentStreak));
				this.statsData.bestStreak = Math.max(...this.habits.map(h => h.bestStreak));
			},

			async prepareChartData() {
				try {
					const token = uni.getStorageSync('token');
					if (!token) throw new Error('未找到 token，请先登录');

					const today = dayjs().format('YYYY-MM-DD');

					// 1. 确定时间范围
					const days = this.selectedTimeRange === '近7天' ? 7 :
						this.selectedTimeRange === '近30天' ? 30 :
							this.selectedTimeRange === '今天' ? 1 : 90;

					// 2. 计算开始和结束日期
					const startDate = dayjs().subtract(days - 1, 'day').format('YYYY-MM-DD');
					const endDate = today;

					// 3. 获取这段时间内的所有习惯日志
					const logsResponse = await axios.post('/habits/logs/range', {
						startDate,
						endDate
					}, {
						headers: { 'satoken': token }
					});

					const logs : Array<{
						habitId : number;
						loggedAt : string;
						isCompleted : boolean;
					}> = logsResponse.data.data || [];

					// 4. 获取当天活跃的习惯列表
					const habitsResponse = await axios.post('/habits/allHabits', {}, {
						headers: { 'satoken': token }
					});

					const habits = habitsResponse.data.data || [];

					// 5. 准备日期范围
					const dateRange = Array.from({ length: days }, (_, i) => {
						return dayjs(startDate).add(i, 'day').format('YYYY-MM-DD');
					});

					// 6. 统计每日完成情况
					const dailyStats : Record<string, {
						totalHabits : number;
						completedHabits : number;
					}> = {};

					dateRange.forEach(date => {
						dailyStats[date] = { totalHabits: 0, completedHabits: 0 };
					});

					// 7. 遍历所有习惯和日期，统计完成情况
					habits.forEach(habit => {
						dateRange.forEach(date => {
							const dayJSDate = dayjs(date);
							const habitStartDate = dayjs(habit.startDate);
							const habitEndDate = dayjs(habit.endDate);

							// 检查日期是否在习惯的有效期内
							if (dayJSDate.isBetween(habitStartDate, habitEndDate, null, '[]')) {
								// 根据习惯频率判断是否需要在这天完成
								let shouldBeCompleted = false;

								switch (habit.frequency) {
									case 1: // 每日
										shouldBeCompleted = true;
										break;
									case 2: // 每周
										shouldBeCompleted = dayJSDate.day() === habitStartDate.day();
										break;
									case 3: // 每月
										shouldBeCompleted = dayJSDate.date() === habitStartDate.date();
										break;
								}

								if (shouldBeCompleted) {
									dailyStats[date].totalHabits++;

									// 查找日志记录
									const log = logs.find(l =>
										l.habitId === habit.id &&
										l.loggedAt === date
									);

									if (log && log.isCompleted) {
										dailyStats[date].completedHabits++;
									}
								}
							}
						});
					});

					// 8. 准备图表数据
					const categories = dateRange.map(date => dayjs(date).format('MM-DD'));
					const seriesData = dateRange.map(date => {
						const { totalHabits, completedHabits } = dailyStats[date];
						return totalHabits > 0 ? Math.round((completedHabits / totalHabits) * 100) : 0;
					});

					this.completionRateChartData = {
						categories,
						series: [{
							name: '完成率',
							data: seriesData,
							color: '#5b8ef3'
						}]
					};

					// 9. 分类分布图
					const categoryCounts = habits.reduce((acc, habit) => {
						acc[habit.category] = (acc[habit.category] || 0) + 1;
						return acc;
					}, {} as Record<string, number>);

					this.categoryDistributionChartData = {
						categories: Object.keys(categoryCounts),
						series: Object.keys(categoryCounts).map(category => ({
							name: category,
							data: categoryCounts[category],
							color: this.getCategoryColor(category)
						}))
					};

				} catch (err) {
					console.error('获取图表数据失败:', err);
					uni.showToast({
						title: '获取统计数据失败',
						icon: 'none'
					});
				}
			},
			tooltipFormat(chart : any, tooltipOpt : any) {
				return {
					textList: [
						{ text: tooltipOpt.title || '', color: '#333', font: '12px' },
						{
							text: `完成率: ${tooltipOpt.data[0].value}%`,
							color: '#5b8ef3',
							font: '14px'
						}
					]
				};
			},
			performanceRankColor(index : number) {
				if (index === 0) return '#FF5252'; // 第一名红色
				if (index === 1) return '#FF9800'; // 第二名橙色
				if (index === 2) return '#4CAF50'; // 第三名绿色
				return '#666'; // 其他灰色
			}

		},
		mounted() {
			this.fetchHabits();
			this.updateIndicatorPosition();
		},
		created() {
			dayjs.extend(isBetween);
		}
	});
</script>

<style>
	/* 保留基本样式 */
	page {
		height: 100%;
	}

	.cursor-pointer {
		cursor: pointer;
	}

	.habit-tracker {
		display: flex;
		flex-direction: column;
		height: 100%;
		background-color: #f8fafc;
		position: relative;
	}

	/* 标签栏样式 */
	.tab-container {
		display: flex;
		position: relative;
		height: 100rpx;
		background-color: #ffffff;
		border-bottom: 1px solid #f0f0f0;
		flex-shrink: 0;
	}

	.tab-item {
		flex: 1;
		display: flex;
		justify-content: center;
		align-items: center;
		position: relative;
	}

	.tab-text {
		font-size: 16px;
		color: #666666;
		transition: color 0.3s;
	}

	.tab-item.active .tab-text {
		color: #5b8ef3;
		font-weight: 500;
	}

	.tab-indicator {
		position: absolute;
		bottom: 0;
		width: 250rpx;
		height: 4rpx;
		background-color: #5b8ef3;
		transition: left 0.3s ease;
	}

	/* 内容区域 */
	.content-swiper {
		flex: 1;
		width: 100%;
	}

	/* 日历视图 */
	.calendar-view {
		padding: 30rpx;
		height: 100%;
		display: flex;
		flex-direction: column;
	}




	/* 列表视图 */
	.list-view {
		height: 100%;
		width: 100%;
		overflow: auto;
		padding: 30rpx;
	}

	/* 今日已完成的习惯卡片样式 */
	.habit-card.completed-today {
		border-left: 8rpx solid #4CAF50;
		background-color: #f1f9f2;
		box-shadow: 0 4rpx 16rpx rgba(76, 175, 80, 0.15);
	}

	.completed-badge {
		position: absolute;
		top: 0;
		right: 0;
		background-color: #4CAF50;
		padding: 6rpx 16rpx;
		border-radius: 0 0 0 16rpx;
		display: flex;
		align-items: center;
	}

	.completed-text {
		font-size: 12px;
		color: #ffffff;
		margin-left: 6rpx;
	}

	.habit-list {
		display: flex;
		flex-direction: column;
		gap: 30rpx;
		width: 395px;
	}

	.habit-card {
		background-color: #ffffff;
		border-radius: 16rpx;
		padding: 30rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
		position: relative;
		overflow: hidden;
	}

	.habit-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
	}

	.habit-name {
		font-size: 18px;
		font-weight: 500;
		color: #333333;
	}

	.habit-category {
		padding: 6rpx 16rpx;
		border-radius: 20rpx;
		background-color: #e3f2fd;
	}

	.category-text {
		font-size: 12px;
		color: #ffffff;
	}

	.habit-streak {
		display: flex;
		margin-bottom: 20rpx;
	}

	.streak-text {
		font-size: 14px;
		color: #666666;
		margin-right: 20rpx;
	}

	.streak-best {
		font-size: 14px;
		color: #999999;
	}

	.habit-progress {
		display: flex;
		align-items: center;
	}

	.progress-bar {
		flex: 1;
		height: 16rpx;
		background-color: #f0f0f0;
		border-radius: 8rpx;
		overflow: hidden;
		margin-right: 20rpx;
	}

	.progress-fill {
		height: 100%;
		background-color: #5b8ef3;
		border-radius: 8rpx;
	}

	.progress-text {
		font-size: 14px;
		color: #666666;
		width: 60rpx;
		text-align: right;
	}

	/* 空状态 */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 0;
	}

	.empty-image {
		width: 300rpx;
		height: 300rpx;
		margin-bottom: 30rpx;
	}

	.empty-text {
		font-size: 16px;
		color: #999999;
		margin-bottom: 30rpx;
	}

	.add-habit-btn {
		background: linear-gradient(135deg, #5b8ef3, #3f6ad8);
		padding: 20rpx 40rpx;
		border-radius: 40rpx;
		box-shadow: 0 6rpx 16rpx rgba(91, 142, 243, 0.3);
	}

	.add-habit-text {
		font-size: 14px;
		color: #ffffff;
	}

	/* 悬浮添加按钮 */
	.floating-btn {
		position: fixed;
		right: 40rpx;
		bottom: 40rpx;
		width: 100rpx;
		height: 100rpx;
		border-radius: 50rpx;
		background: linear-gradient(135deg, #5b8ef3, #3f6ad8);
		display: flex;
		justify-content: center;
		align-items: center;
		box-shadow: 0 6rpx 16rpx rgba(91, 142, 243, 0.3);
		z-index: 99;
	}

	/* 习惯操作菜单 */
	.habit-actions-overlay {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.5);
		z-index: 300;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.habit-actions-modal {
		width: 560rpx;
		background-color: #ffffff;
		border-radius: 24rpx;
		padding: 30rpx;
		display: flex;
		flex-direction: column;
	}

	.habit-actions-title {
		font-size: 18px;
		font-weight: 500;
		color: #333333;
		margin-bottom: 30rpx;
		text-align: center;
		padding-bottom: 20rpx;
		border-bottom: 1px solid #f0f0f0;
	}

	.habit-actions-buttons {
		display: flex;
		flex-direction: column;
	}

	.habit-action-item {
		display: flex;
		align-items: center;
		padding: 24rpx 20rpx;
		border-bottom: 1px solid #f0f0f0;
	}

	.habit-action-item:last-child {
		border-bottom: none;
	}

	.habit-action-text {
		font-size: 16px;
		color: #333333;
		margin-left: 20rpx;
	}

	/* 删除确认模态框 */
	.confirm-delete-overlay {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.5);
		z-index: 300;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.confirm-delete-modal {
		width: 560rpx;
		background-color: #ffffff;
		border-radius: 24rpx;
		padding: 40rpx;
		display: flex;
		flex-direction: column;
	}

	.confirm-delete-title {
		font-size: 18px;
		font-weight: 500;
		color: #333333;
		margin-bottom: 20rpx;
	}

	.confirm-delete-message {
		font-size: 14px;
		color: #666666;
		margin-bottom: 40rpx;
	}

	.confirm-delete-buttons {
		display: flex;
		justify-content: flex-end;
		gap: 20rpx;
	}

	.confirm-btn {
		padding: 16rpx 30rpx;
		border-radius: 8rpx;
		font-size: 14px;
	}

	.cancel-btn {
		background-color: #f0f0f0;
		color: #666666;
	}

	.delete-confirm-btn {
		background-color: #FF5252;
		color: #ffffff;
	}

	/* 编辑习惯模态框样式 */
	.edit-modal-overlay {
		position: fixed;
		top: 0;
		left: 0;
		width: 100%;
		height: 100%;
		background-color: rgba(0, 0, 0, 0.5);
		z-index: 400;
		display: flex;
		justify-content: center;
		align-items: center;
	}

	.edit-modal-container {
		width: 600rpx;
		background-color: #ffffff;
		border-radius: 24rpx;
		padding: 40rpx;
		box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
	}

	.modal-title {
		font-size: 18px;
		font-weight: 500;
		color: #333333;
		margin-bottom: 30rpx;
		display: block;
		text-align: center;
	}

	.form-item {
		margin-bottom: 30rpx;
	}

	.form-row {
		display: flex;
		justify-content: space-between;
	}

	.date-item {
		width: 48%;
	}

	.form-label {
		font-size: 14px;
		color: #666666;
		margin-bottom: 10rpx;
		display: block;
	}

	.form-input,
	.form-textarea,
	.picker-view {
		width: 90%;
		border: 1px solid #f0f0f0;
		border-radius: 8rpx;
		padding: 20rpx;
		font-size: 14px;
		color: #333333;
		background-color: #fff;
	}

	.form-input {
		height: 10rpx;
	}

	.form-textarea {
		height: 100rpx;
	}

	.picker-view {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.modal-actions {
		display: flex;
		justify-content: flex-end;
		margin-top: 40rpx;
	}

	.modal-btn {
		padding: 16rpx 32rpx;
		border-radius: 8rpx;
		font-size: 14px;
		margin-left: 20rpx;
		cursor: pointer;
	}

	.cancel-btn {
		background-color: #f0f0f0;
		color: #666666;
	}

	.confirm-btn {
		background-color: #5b8ef3;
		color: #ffffff;
	}

	.habit-category-frequency {
		position: relative;
		top: 7px;
		display: flex;
		align-items: center;
	}

	.frequency-badge {
		background-color: #9575CD;
		padding: 4rpx 12rpx;
		border-radius: 10rpx;
		margin-left: 10rpx;
	}

	.frequency-text {
		font-size: 12px;
		color: #FFFFFF;
	}

	/* 日期头部样式 */
	.date-header {
		margin-bottom: 20rpx;
		padding: 0 30rpx;
	}

	.date-text {
		font-size: 18px;
		font-weight: 500;
		color: #333333;
		margin-right: 16rpx;
	}

	.weekday-text {
		font-size: 16px;
		color: #666666;
	}

	.stats-view {
		height: 100%;
		width: 100%;
		background-color: #f8fafc;
	}

	.stats-container {
		padding: 30rpx;
	}

	.stats-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 30rpx;
	}

	.stats-title {
		font-size: 20px;
		font-weight: 600;
		color: #333;
	}

	.time-selector {
		position: relative;
		padding: 10rpx 20rpx;
		background-color: #fff;
		border-radius: 40rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
		display: flex;
		align-items: center;
	}

	.time-text {
		font-size: 14px;
		color: #666;
		margin-right: 10rpx;
	}

	.time-dropdown {
		position: absolute;
		top: 100%;
		right: 0;
		margin-top: 10rpx;
		background-color: #fff;
		border-radius: 12rpx;
		box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.1);
		z-index: 10;
		min-width: 200rpx;
	}

	.time-option {
		padding: 20rpx 30rpx;
		border-bottom: 1px solid #f8f8f8;
	}

	.time-option:last-child {
		border-bottom: none;
	}

	.time-option.active {
		color: #5b8ef3;
	}

	.stats-summary {
		display: flex;
		justify-content: space-between;
		margin-bottom: 30rpx;
	}

	.stats-card {
		flex: 1;
		margin: 0 10rpx;
		padding: 30rpx 20rpx;
		border-radius: 16rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.stats-value {
		font-size: 24px;
		font-weight: 600;
		color: #fff;
		margin-bottom: 10rpx;
	}

	.stats-label {
		font-size: 12px;
		color: rgba(255, 255, 255, 0.9);
	}

	.stats-chart {
		background-color: #fff;
		border-radius: 16rpx;
		padding: 30rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	}

	.chart-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
	}

	.chart-title {
		font-size: 16px;
		font-weight: 500;
		color: #333;
	}

	.chart-legend {
		display: flex;
		align-items: center;
	}

	.legend-item {
		display: flex;
		align-items: center;
		margin-left: 20rpx;
	}

	.legend-color {
		width: 24rpx;
		height: 24rpx;
		border-radius: 4rpx;
		margin-right: 10rpx;
	}

	.legend-text {
		font-size: 12px;
		color: #666;
	}

	.habit-performance {
		background-color: #fff;
		border-radius: 16rpx;
		padding: 30rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	}

	.performance-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
	}

	.performance-title {
		font-size: 16px;
		font-weight: 500;
		color: #333;
	}

	.performance-filter {
		display: flex;
		align-items: center;
		color: #666;
		font-size: 12px;
	}

	.performance-item {
		display: flex;
		align-items: center;
		padding: 20rpx 0;
		border-bottom: 1px dashed #f0f0f0;
	}

	.performance-item:last-child {
		border-bottom: none;
	}

	.performance-rank {
		width: 40rpx;
		font-size: 16px;
		font-weight: 600;
		text-align: center;
		margin-right: 20rpx;
	}

	.performance-info {
		flex: 1;
	}

	.performance-name {
		font-size: 14px;
		color: #333;
		margin-bottom: 6rpx;
		display: block;
	}

	.performance-meta {
		display: flex;
		align-items: center;
	}

	.performance-category {
		font-size: 10px;
		color: #fff;
		padding: 4rpx 12rpx;
		border-radius: 20rpx;
		margin-right: 10rpx;
	}

	.performance-streak {
		font-size: 12px;
		color: #999;
	}

	.performance-value {
		font-size: 16px;
		font-weight: 600;
		color: #5b8ef3;
	}

	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 0;
	}

	.empty-image {
		width: 300rpx;
		height: 300rpx;
		margin-bottom: 30rpx;
	}

	.empty-text {
		font-size: 16px;
		color: #999;
		margin-bottom: 30rpx;
	}

	.add-habit-btn {
		background: linear-gradient(135deg, #5b8ef3, #3f6ad8);
		padding: 20rpx 40rpx;
		border-radius: 40rpx;
		box-shadow: 0 6rpx 16rpx rgba(91, 142, 243, 0.3);
	}

	.add-habit-text {
		font-size: 14px;
		color: #fff;
	}

	/*  */
	.stats-view {
		height: 100%;
		width: 100%;
		background-color: #f8fafc;
	}

	.stats-container {
		padding: 30rpx;
	}

	.stats-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 30rpx;
	}

	.stats-title {
		font-size: 20px;
		font-weight: 600;
		color: #333;
	}

	.time-selector {
		position: relative;
		padding: 10rpx 20rpx;
		background-color: #fff;
		border-radius: 40rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
		display: flex;
		align-items: center;
	}

	.time-text {
		font-size: 14px;
		color: #666;
		margin-right: 10rpx;
	}

	.time-dropdown {
		position: absolute;
		top: 100%;
		right: 0;
		margin-top: 10rpx;
		background-color: #fff;
		border-radius: 12rpx;
		box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.1);
		z-index: 10;
		min-width: 200rpx;
	}

	.time-option {
		padding: 20rpx 30rpx;
		border-bottom: 1px solid #f8f8f8;
	}

	.time-option:last-child {
		border-bottom: none;
	}

	.time-option.active {
		color: #5b8ef3;
	}

	.stats-summary {
		display: flex;
		justify-content: space-between;
		margin-bottom: 30rpx;
	}

	.stats-card {
		flex: 1;
		margin: 0 10rpx;
		padding: 30rpx 20rpx;
		border-radius: 16rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.stats-value {
		font-size: 24px;
		font-weight: 600;
		color: #fff;
		margin-bottom: 10rpx;
	}

	.stats-label {
		font-size: 12px;
		color: rgba(255, 255, 255, 0.9);
	}

	.stats-chart {
		background-color: #fff;
		border-radius: 16rpx;
		padding: 30rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	}

	.chart-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
	}

	.chart-title {
		font-size: 16px;
		font-weight: 500;
		color: #333;
	}

	.chart-legend {
		display: flex;
		align-items: center;
	}

	.legend-item {
		display: flex;
		align-items: center;
		margin-left: 20rpx;
	}

	.legend-color {
		width: 24rpx;
		height: 24rpx;
		border-radius: 4rpx;
		margin-right: 10rpx;
	}

	.legend-text {
		font-size: 12px;
		color: #666;
	}

	.habit-performance {
		background-color: #fff;
		border-radius: 16rpx;
		padding: 30rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	}

	.performance-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 20rpx;
	}

	.performance-title {
		font-size: 16px;
		font-weight: 500;
		color: #333;
	}

	.performance-filter {
		display: flex;
		align-items: center;
		color: #666;
		font-size: 12px;
	}

	.performance-item {
		display: flex;
		align-items: center;
		padding: 20rpx 0;
		border-bottom: 1px dashed #f0f0f0;
	}

	.performance-item:last-child {
		border-bottom: none;
	}

	.performance-rank {
		width: 40rpx;
		font-size: 16px;
		font-weight: 600;
		text-align: center;
		margin-right: 20rpx;
	}

	.performance-info {
		flex: 1;
	}

	.performance-name {
		font-size: 14px;
		color: #333;
		margin-bottom: 6rpx;
		display: block;
	}

	.performance-meta {
		display: flex;
		align-items: center;
	}

	.performance-category {
		font-size: 10px;
		color: #fff;
		padding: 4rpx 12rpx;
		border-radius: 20rpx;
		margin-right: 10rpx;
	}

	.performance-streak {
		font-size: 12px;
		color: #999;
	}

	.performance-value {
		font-size: 16px;
		font-weight: 600;
		color: #5b8ef3;
	}

	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 0;
	}

	.empty-image {
		width: 300rpx;
		height: 300rpx;
		margin-bottom: 30rpx;
	}

	.empty-text {
		font-size: 16px;
		color: #999;
		margin-bottom: 30rpx;
	}

	.add-habit-btn {
		background: linear-gradient(135deg, #5b8ef3, #3f6ad8);
		padding: 20rpx 40rpx;
		border-radius: 40rpx;
		box-shadow: 0 6rpx 16rpx rgba(91, 142, 243, 0.3);
	}

	.add-habit-text {
		font-size: 14px;
		color: #fff;
	}
</style>