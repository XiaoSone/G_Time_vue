<template>
	<view class="page" @touchstart="closeAll">
		<!-- 顶部导航栏 -->
		<view class="header">
			<text class="title">任务清单</text>
			<view class="search-container">
				<input class="search-input" v-model="searchKeyword" placeholder="搜索..." confirm-type="search"
					@confirm="handleSearch" />
				<uni-icons v-if="searchKeyword" class="search-clear" type="clear" size="18" @tap="resetSearch" />
			</view>
			<view class="sort-container" @tap.stop="handleSetting">
				<uni-icons class="cursor-pointer" type="gear" size="22" />
				<!-- 排序下拉菜单 -->
				<view class="sort-dropdown" v-if="showSortDropdown" @tap.stop>
					<view class="sort-option" @tap="handleSortChange('priority')">
						<text>优先级排列</text>
						<view class="sort-indicator">
							<uni-icons v-if="sortType === 'priority'"
								:type="sortOrder === 'desc' ? 'arrow-down' : 'arrow-up'" size="14"
								:color="sortOrder === 'desc' ? '#4080FF' : '#4080FF'" />
						</view>
					</view>
					<view class="sort-option" @tap="handleSortChange('endTime')">
						<text>截止时间排列</text>
						<view class="sort-indicator">
							<uni-icons v-if="sortType === 'endTime'"
								:type="sortOrder === 'desc' ? 'arrow-down' : 'arrow-up'" size="14"
								:color="sortOrder === 'desc' ? '#4080FF' : '#4080FF'" />
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>

	<!-- 日期显示 -->
	<view class="date">{{time}}</view>

	<!-- 分类标签栏 -->
	<view class="category-container">
		<!-- 第一行：显示其他分类 -->
		<view class="category-row">
			<view v-for="(item, index) in otherCategories" :key="index"
				:class="['category-item', currentCategory === item.key ? 'active' : '']"
				@click="handleCategoryChange(item.key)">
				<text>{{ item.name }}</text>
				<text v-if="item.count > 0" class="category-count">{{ item.count }}</text>
			</view>
		</view>
		<!-- 第二行：显示 "全部" 和 "已完成" -->
		<view class="category-row">
			<view v-for="(item, index) in firstRowCategories" :key="index"
				:class="['category-item', currentCategory === item.key ? 'active' : '']"
				@click="handleCategoryChange(item.key)">
				<text>{{ item.name }}</text>
				<text v-if="item.count > 0" class="category-count">{{ item.count }}</text>
			</view>
		</view>
	</view>

	<!-- 任务卡片列表 -->
	<scroll-view class="task-list" scroll-y>
		<view v-for="(task, index) in filteredTasks" :key="index" class="swipe-container">
			<view class="task-card" :class="{ 'expired-task': task.isExpired, 'near-expired-task': task.isNearExpired }"
				:style="{ transform: `translateX(${task.offsetX}px)` ,borderLeft: task.isExpired ? '4px solid #FF6B6B' : 
               (task.isNearExpired ? '4px solid #FFC107' : '4px solid transparent')}"
				@touchstart.stop="handleTouchStart($event, index)" @touchmove.stop="handleTouchMove($event, index)"
				@touchend.stop="handleTouchEnd($event, index)">
				<view class="task-content">
					<view :class="['priority-dot', `priority-${task.priority}`]"></view>
					<view class="task-info">
						<text class="task-title">{{ task.title }}</text>
						<text class="task-description">{{ task.description }}</text>
						<view class="task-meta">
							<text :class="['category-tag', task.category]">
								{{ getCategoryName(task.category) }}
							</text>
							<text class="task-time">{{ task.endTime }}</text>
						</view>
					</view>
					<view class="status-tag" v-if="task.notStartedStatus == 1">未开始</view>
					<uni-icons type="right" size="16" color="#999" v-if="task.actionsTranslateX !== -180" />
				</view>
			</view>

			<!-- 操作按钮区域 -->
			<view class="actions" :style="{transform:`translateX(${task.actionsTranslateX }px)`}">
				<button class="edit-btn" @tap="handleEdit(task)" @touchstart.stop>编辑</button>
				<button class="delete-btn" @touchstart.stop="handleDelete(task)">删除</button>
			</view>
		</view>
	</scroll-view>

	<!-- 添加任务按钮 -->
	<view class="add-button cursor-pointer" @tap="handleAddTask">
		<uni-icons type="plus" size="24" color="#FFFFFF" />
	</view>

	<!-- 编辑弹窗 -->
	<uni-popup ref="editPopup" type="center">
		<view class="edit-modal">
			<view class="modal-header">
				<text>编辑任务</text>
				<uni-icons type="closeempty" size="20" @click="closeEditPopup" class="close-icon"></uni-icons>
			</view>

			<view class="form-item">
				<text class="form-label">任务标题</text>
				<input v-model="editForm.title" class="form-input" placeholder="输入任务标题" />
			</view>

			<view class="form-item">
				<text class="form-label">任务描述</text>
				<textarea v-model="editForm.description" class="form-textarea" placeholder="输入任务描述"></textarea>
			</view>

			<view class="form-item">
				<text class="form-label">任务分类</text>
				<picker :value="categoryPickerIndex" :range="categoryOptions">
					<view class="picker">
						{{ editForm.category ? getCategoryDisplayName(editForm.category) : '请选择分类' }}
					</view>
				</picker>
			</view>

			<view class="form-item">
				<text class="form-label">任务优先级</text>
				<picker @change="onPriorityChange" :value="priorityPickerIndex" :range="priorityOptions">
					<view class="picker">
						{{ editForm.priority ? getPriorityDisplayName(editForm.priority) : '请选择优先级' }}
					</view>
				</picker>
			</view>

			<view class="form-item">
				<text class="form-label">开始时间</text>
				<uni-datetime-picker disabled=true v-model=editForm.startTime @change="onDateChange" />
			</view>

			<view class="form-item">
				<text class="form-label">截止时间</text>
				<uni-datetime-picker v-model=editForm.endTime :start="new Date().toISOString().split('T')[0]"
					@change="onDateChange" />
			</view>

			<button class="save-btn" @click="saveTask">保存</button>
		</view>
	</uni-popup>
	<!-- 添加任务弹窗 -->
	<uni-popup ref="addPopup" type="center">
		<view class="edit-modal">
			<view class="modal-header">
				<text>添加新任务</text>
				<uni-icons type="closeempty" size="20" @click="closeAddPopup" class="close-icon"></uni-icons>
			</view>
			<view class="form-item">
				<text class="form-label">任务标题</text>
				<input v-model="addForm.title" class="form-input" placeholder="输入任务标题" />
			</view>
			<view class="form-item">
				<text class="form-label">任务描述</text>
				<textarea v-model="addForm.description" class="form-textarea" placeholder="输入任务描述"></textarea>
			</view>
			<view class="form-item">
				<text class="form-label">任务分类</text>
				<picker @change="onAddCategoryChange" :value="addCategoryPickerIndex" :range="categoryOptions">
					<view class="picker">
						{{ addForm.category ? addForm.category : '请选择分类' }}
					</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">任务优先级</text>
				<picker @change="onAddPriorityChange" :value="addPriorityPickerIndex" :range="priorityOptions">
					<view class="picker">
						{{ addForm.priority ? getPriorityDisplayName(addForm.priority) : '请选择优先级' }}
					</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">开始时间</text>
				<uni-datetime-picker v-model="addForm.startTime" @change="onAddDateChange" />
			</view>
			<view class="form-item">
				<text class="form-label">截止时间</text>
				<uni-datetime-picker v-model="addForm.endTime" :start="new Date().toISOString().split('T')[0]"
					@change="onAddDateChange" />
			</view>
			<button class="save-btn" @click="addTask">保存</button>
		</view>
	</uni-popup>


</template>

<script>
	import axios from 'axios';
	import dayjs from 'dayjs';

	export default {
		data() {
			return {
				time: "",
				currentCategory: 'all',
				categories: [{
						key: 'all',
						name: '全部',
						count: 0
					},
					{
						key: 'work',
						name: '工作',
						count: 0
					},
					{
						key: 'personal',
						name: '个人',
						count: 0
					},
					{
						key: 'study',
						name: '学习',
						count: 0
					},
					{
						key: 'health',
						name: '健康',
						count: 0
					},
					{
						key: 'play',
						name: '娱乐',
						count: 0
					},
					{
						key: 'finish',
						name: '已完成',
						count: 0
					},
					{
						key: 'expired',
						name: '过期',
						count: 0
					}
				],
				tasks: [],
				startX: 0,
				currentIndex: null, // 当前展开卡片的索引
				editForm: {
					id: null,
					title: '',
					description: '',
					category: '',
					priority: '',
					startTime: '',
					endTime: ''
				},
				categoryOptions: ['工作', '个人', '学习', '健康', '娱乐'],
				priorityOptions: ['高', '中', '低'],
				categoryPickerIndex: 0,
				priorityPickerIndex: 0,
				editingTaskId: null,

				addForm: {
					title: '',
					description: '',
					category: '',
					priority: '2',
					startTime: dayjs().format('YYYY-MM-DDTHH:mm:ss'),
					endTime: ''
				},
				addCategoryPickerIndex: 0,
				addPriorityPickerIndex: 2,
				searchKeyword: '',

				showSortDropdown: false,
				sortType: 'priority', // 'priority' 或 'endTime'
				sortOrder: 'desc',
			};
		},
		computed: {
			filteredTasks() {
				let filtered = [];
				const now = dayjs();
				const warningThreshold = dayjs().add(2, 'day'); // 2天后过期的任务将显示警告

				if (this.currentCategory === 'all') {
					filtered = this.tasks.filter(task =>
						task.status != 1 && dayjs(task.endTime).isAfter(now)
					);
				} else if (this.currentCategory === 'finish') {
					filtered = this.tasks.filter(task => task.status === 1);
				} else if (this.currentCategory === 'expired') {
					filtered = this.tasks.filter(task =>
						task.status != 1 && dayjs(task.endTime).isBefore(now)
					);
				} else {
					filtered = this.tasks.filter(task =>
						task.category === this.currentCategory &&
						task.status != 1 &&
						dayjs(task.endTime).isAfter(now)
					);
				}

				// 模糊搜索逻辑
				if (this.searchKeyword.trim()) {
					const keyword = this.searchKeyword.toLowerCase();
					filtered = filtered.filter(task =>
						(task.title && task.title.toLowerCase().includes(keyword)) ||
						(task.description && task.description.toLowerCase().includes(keyword))
					);
				}

				return filtered.sort((a, b) => {
					if (this.sortType === 'priority') {
						// 优先级排序
						const priorityMap = {
							high: 3,
							medium: 2,
							low: 1
						};
						const aVal = priorityMap[a.priority] || 0;
						const bVal = priorityMap[b.priority] || 0;
						return this.sortOrder === 'desc' ? bVal - aVal : aVal - bVal;
					} else {
						// 截止时间排序
						const aTime = new Date(a.endTime).getTime();
						const bTime = new Date(b.endTime).getTime();
						return this.sortOrder === 'desc' ? bTime - aTime : aTime - bTime;
					}
				});;
			},
			firstRowCategories() {
				return this.displayedCategories.filter(item => item.key === 'all' || item.key === 'finish' || item.key ===
					'expired');
			},
			otherCategories() {
				return this.displayedCategories.filter(item => item.key !== 'all' && item.key !== 'finish' && item.key !==
					'expired');
			},
			displayedCategories() {
				if (!this.searchKeyword.trim()) {
					return this.categories;
				}

				const keyword = this.searchKeyword.toLowerCase();
				const now = dayjs();

				const searchResults = this.tasks.filter(task => {
					return (
						(task.title && task.title.toLowerCase().includes(keyword)) ||
						(task.description && task.description.toLowerCase().includes(keyword))
					) && (task.status === 1 || dayjs(task.endTime).isAfter(now));
				});

				return this.categories.map(category => {
					if (category.key === 'all') {
						return {
							...category,
							count: searchResults.filter(task => task.status != 1).length
						};
					} else if (category.key === 'finish') {
						return {
							...category,
							count: searchResults.filter(task => task.status === 1).length
						};
					} else {
						return {
							...category,
							count: searchResults.filter(task =>
								task.category === category.key && task.status != 1
							).length
						};
					}
				});
			},
			

		},
		methods: {
			handleCategoryChange(category) {
				this.currentCategory = category;
				this.currentIndex = null;
			},
			getCategoryName(category) {
				const categoryItem = this.categories.find(item => item.key === category);
				return categoryItem ? categoryItem.name : '';
			},
			// 处理滑动开始
			handleTouchStart(e, index) {
				this.startX = e.touches[0].clientX;
				this.currentIndex = index;
				this.filteredTasks.forEach((task, i) => {
					if (i !== index) {
						task.offsetX = 0;
						task.actionsTranslateX = 0;
					}
				});
				// this.startX = e.touches[0].clientX;
				// this.currentTaskId = this.filteredTasks[index].id; // 改为记录ID
				// this.tasks.forEach(task => {
				// 	if (task.id !== this.currentTaskId) {
				// 		task.offsetX = 0;
				// 		task.actionsTranslateX = 0;
				// 	}
				// });
			},
			// 处理滑动中
			handleTouchMove(e, index) {
				if (this.currentIndex !== index) return;
				const currentX = e.touches[0].clientX;
				const deltaX = currentX - this.startX;
				// console.log(deltaX);

				// 允许左右滑动，但限制范围
				if (deltaX < 0) { // 向左滑动
					this.filteredTasks[index].offsetX = Math.max(deltaX, -160);
					this.filteredTasks[index].actionsTranslateX = Math.max(deltaX, -180);
				} else { // 向右滑动
					this.filteredTasks[index].offsetX = Math.min(deltaX, 80);
				}
				// this.$set(this.tasks[taskIndex], 'offsetX', Math.max(Math.min(deltaX, 80), -160));
				// this.$set(this.tasks[taskIndex], 'actionsTranslateX', Math.max(deltaX, -180));
			},
			// 处理滑动结束
			async handleTouchEnd(e, index) {
				if (this.currentIndex !== index) return;
				// 
				// const taskIndex = this.tasks.findIndex(t => t.id === this.currentTaskId);
				// if (taskIndex === -1) return;

				const deltaX = e.changedTouches[0].clientX - this.startX;
				const task = this.filteredTasks[index];
				// const task = this.tasks[taskIndex];

				console.log(deltaX);

				// 向左滑动超过阈值，显示操作按钮
				if (deltaX < -80) {
					this.filteredTasks[index].offsetX = -160;
					this.filteredTasks[index].actionsTranslateX = -180;
					// this.$set(task, 'offsetX', -160);
					// this.$set(task, 'actionsTranslateX', -180);
				}
				// 向右滑动超过阈值，弹出确认对话框
				else if (deltaX > 80) {
					this.filteredTasks[index].actionsTranslateX = 0;
					// this.$set(task, 'actionsTranslateX', 0);

					try {
						const action = task.status === 1 ? '取消完成' : '完成';
						const confirm = await new Promise((resolve) => {
							uni.showModal({
								title: `确认${action}任务`,
								content: `确认${action}任务"${task.title}"吗？`,
								success: res => resolve(res.confirm),
								complete: () => {
									// 无论确认与否，都恢复卡片位置
									this.filteredTasks[index].offsetX = 0;
									this.filteredTasks[index].actionsTranslateX = 0;
									// this.$set(task, 'offsetX', 0);
									// this.$set(task, 'actionsTranslateX', 0);
								}
							});
						});

						if (confirm) {
							await this.completeTask(index);
						}
					} catch (error) {
						console.error('确认操作出错:', error);
					}
				}
				// 滑动不足阈值，恢复原位
				else {
					this.filteredTasks[index].offsetX = 0;
					this.filteredTasks[index].actionsTranslateX = 0;
					// this.$set(task, 'offsetX', 0);
					// this.$set(task, 'actionsTranslateX', 0);
				}
				this.currentIndex = null;
			},
			// 处理编辑
			handleEdit(task) {
				uni.showToast({
					title: `编辑任务：${task.title}`,
					icon: 'none'
				});
			},
			// 处理删除
			async handleDelete(task) {
				const index = this.filteredTasks.findIndex(t => t === task);
				console.log(index);


				try {
					const confirm = await new Promise((resolve) => {
						uni.showModal({
							title: '确认删除',
							content: `确认删除任务"${task.title}"吗？`,
							success: res => resolve(res.confirm)
						});
					});

					if (!confirm) return;

					const token = uni.getStorageSync('token');
					const {
						data
					} = await axios.post(`/task/delete/${task.id}`, {}, {
						headers: {
							satoken: token
						}
					});

					if (data.code === 200) {
						uni.showToast({
							title: '任务已删除',
							icon: 'success'
						});
						this.$refs.editPopup.close();
						this.fetchData();
						this.filteredTasks[index].offsetX = 0;
						this.filteredTasks[index].actionsTranslateX = 0;
					} else {
						uni.showToast({
							title: data.msg || '更新失败',
							icon: 'none'
						});
					}
				} catch (error) {
					uni.showToast({
						title: '请求出错',
						icon: 'none'
					});
					console.error('删除失败:', error);
				}
			},

			// 收起所有卡片
			closeAll() {
				this.tasks.forEach(task => {
					task.offsetX = 0;
					task.actionsTranslateX = 0;
				});
			},
			async fetchData() {
				this.categories = this.categories.map(cat => {
					return {
						...cat,
						count: 0,
					};
				});
				const categoryMap = {
					"工作": "work",
					"个人": "personal",
					"学习": "study",
					"健康": "health",
					"娱乐": "play"
				};

				const priorityMap = {
					3: "high",
					2: "medium",
					1: "low"
				};

				const token = uni.getStorageSync('token');
				const headers = {
					satoken: token
				};
				const tasksResponse = await axios.post("/task/list", {}, {
					headers
				});
				const now = dayjs().format('YYYY-MM-DDTHH:mm:ss');
				const warningThreshold = dayjs().add(2, 'day').format('YYYY-MM-DDTHH:mm:ss');
				// tasksResponse.data.data = displayednotStartedStatus(tasksResponse.data.data);
				this.tasks = tasksResponse.data.data.map(task => {
					const endTime = dayjs(task.endTime);
					return {
						...task,
						offsetX: 0,
						actionsTranslateX: 0,
						isExpired: endTime.isBefore(now) && task.status != 1,
						isNearExpired: !endTime.isBefore(now) && endTime.isBefore(warningThreshold) && task
							.status != 1,
						category: categoryMap[task.category] || task.category, // 如果找不到映射，使用原始值
						priority: priorityMap[task.priority] || task.priority, // 如果找不到映射，使用原始值
						notStartedStatus: task.startTime && dayjs(task.startTime).isAfter(now)
					};
				});

				this.tasks.forEach(task => {
					const category = this.categories.find(cat => cat.key === task.category);
					const isExpired = dayjs(task.endTime).isBefore(now) && task.status != 1;
					const isActive = !isExpired && task.status != 1;

					if (category && isActive) {
						category.count += 1;
					}

					// 更新过期任务计数
					if (isExpired) {
						const expiredCat = this.categories.find(cat => cat.key === 'expired');
						if (expiredCat) expiredCat.count += 1;
					}
				});

				// 更新 '全部' 分类的计数
				const allCategory = this.categories.find(cat => cat.key === 'all');
				if (allCategory) {
					allCategory.count = this.tasks.filter(task => task.status != 1 && dayjs(task.endTime).isAfter(now))
						.length;
				}

				const finishCategory = this.categories.find(cat => cat.key === 'finish');
				finishCategory.count = this.tasks.filter(task => task.status === 1).length;


			},

			// 打开编辑模态框
			handleEdit(task) {
				this.editingTaskId = task.id;
				const priorityNumberMap = {
					'high': '3',
					'medium': '2',
					'low': '1'
				};
				const priorityNumber = priorityNumberMap[task.priority] || task.priority;

				this.editForm = {
					id: task.id,
					title: task.title,
					description: task.description,
					category: this.getCategoryDisplayName(task.category),
					priority: priorityNumber,
					startTime: task.startTime,
					endTime: task.endTime
				};
				this.$refs.editPopup.open();
			},
			// 关闭编辑模态框
			closeEditPopup() {
				this.$refs.editPopup.close();
			},
			// 获取分类显示名称
			getCategoryDisplayName(key) {
				const map = {
					'work': '工作',
					'personal': '个人',
					'study': '学习',
					'health': '健康',
					'play': '娱乐'
				};
				return map[key] || key;
			},

			// 获取优先级显示名称
			getPriorityDisplayName(key) {
				const map = {
					'3': '高',
					'2': '中',
					'1': '低'
				};
				return map[key.toString()] || key;
			},

			// 优先级选择器变化
			onPriorityChange(e) {
				const index = e.detail.value;
				this.priorityPickerIndex = index;
				const keyMap = {
					'高': '3',
					'中': '2',
					'低': '1'
				};
				this.editForm.priority = keyMap[this.priorityOptions[index]];
			},

			// 日期选择器变化
			onDateChange(e) {
				this.editForm.endTime = e.detail.value;
			},

			// 保存任务
			async saveTask() {
				console.log(this.editForm);
				if (!this.editForm.title.trim()) {
					uni.showToast({
						title: '请填写任务标题',
						icon: 'none'
					});
					return;
				}

				try {
					const token = uni.getStorageSync('token');
					const headers = {
						satoken: token
					};

					const response = await axios.post(`/task/update/${this.editingTaskId}`, {
						...this.editForm,
						category: this.editForm.category,
						priority: this.editForm.priority
					}, {
						headers
					});

					if (response.data.code === 200) {
						uni.showToast({
							title: '任务更新成功',
							icon: 'success'
						});
						this.$refs.editPopup.close();
						this.fetchData(); // 刷新列表
					} else {
						uni.showToast({
							title: response.data.msg || '更新失败',
							icon: 'none'
						});
					}
				} catch (error) {
					uni.showToast({
						title: '请求失败',
						icon: 'none'
					});
					console.error('更新任务失败:', error);
				}
			},

			// 处理添加任务按钮点击
			handleAddTask() {
				this.addForm = { // 重置表单
					title: '',
					description: '',
					category: '',
					priority: '2',
					startTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
					endTime: ''
				};
				this.$refs.addPopup.open();
			},

			// 关闭添加任务弹窗
			closeAddPopup() {
				this.$refs.addPopup.close();
			},

			// 添加任务分类选择变化
			onAddCategoryChange(e) {
				this.addCategoryPickerIndex = e.detail.value;
				this.addForm.category = this.categoryOptions[this.addCategoryPickerIndex];
			},

			// 添加任务优先级选择变化
			onAddPriorityChange(e) {
				const index = e.detail.value;
				this.addPriorityPickerIndex = index;
				this.addForm.priority = ['3', '2', '1'][index]; // 对应高、中、低
			},

			// 添加任务日期变化
			onAddDateChange(e) {
				this.addForm.endTime = e.detail.value;
			},

			// 添加任务
			async addTask() {
				if (!this.addForm.title.trim()) {
					uni.showToast({
						title: '请填写任务标题',
						icon: 'none'
					});
					return;
				}

				if (!this.addForm.category) {
					uni.showToast({
						title: '请选择任务分类',
						icon: 'none'
					});
					return;
				}

				try {
					const token = uni.getStorageSync('token');
					const {
						data
					} = await axios.post('/task/create', this.addForm, {
						headers: {
							satoken: token
						}
					});

					if (data.code === 200) {
						uni.showToast({
							title: '任务添加成功',
							icon: 'success'
						});
						this.$refs.addPopup.close();
						this.fetchData(); // 刷新任务列表
					} else {
						uni.showToast({
							title: data.msg || '添加失败',
							icon: 'none'
						});
					}
				} catch (error) {
					uni.showToast({
						title: '请求失败',
						icon: 'none'
					});
					console.error('添加任务失败:', error);
				}
			},

			handleSearch() {
				if (this.searchKeyword.trim()) {
					// 搜索状态下自动切换到"全部"分类
					this.currentCategory = 'all';
				}
				this.currentIndex = null;
			},

			resetSearch() {
				this.searchKeyword = '';
				this.currentIndex = null;
				// 清空搜索后恢复到原始计数
				this.fetchData();
			},

			// 完成任务
			async completeTask(index) {
				const task = this.filteredTasks[index];
				if (!task) return;

				// 创建 category 映射关系
				const categoryMap = {
					'work': '工作',
					'personal': '个人',
					'study': '学习',
					'health': '健康',
					'play': '娱乐'
				};

				// 创建 priority 映射关系
				const priorityMap = {
					'high': 3,
					'medium': 2,
					'low': 1
				};

				try {
					const token = uni.getStorageSync('token');
					// 准备发送给后端的数据，注意转换数据类型
					const postData = {
						...task,
						category: categoryMap[task.category] || task.category, // 转换为中文
						priority: priorityMap[task.priority] || task.priority, // 转换为数字
						status: task.status === 1 ? 0 : 1 // 如果是已完成的任务则取消完成，否则标记为已完成
					};

					const {
						data
					} = await axios.post(
						`/task/update/${task.id}`,
						postData, {
							headers: {
								satoken: token
							}
						}
					);

					if (data.code === 200) {
						const action = task.status === 1 ? "取消完成" : "完成";
						uni.showToast({
							title: `任务 "${task.title}" 已${action}`,
							icon: 'success'
						});
						this.fetchData();
					}
				} catch (error) {
					console.error('操作任务失败:', error);
					uni.showToast({
						title: '操作任务失败',
						icon: 'none'
					});
				}
			},

			handleSetting() {
				this.showSortDropdown = !this.showSortDropdown;
			},
			handleSortChange(type) {
				if (this.sortType === type) {
					// 如果点击的是当前排序类型，则切换升序降序
					this.sortOrder = this.sortOrder === 'desc' ? 'asc' : 'desc';
				} else {
					// 如果点击的是不同排序类型，则设置为该类型，默认降序
					this.sortType = type;
					this.sortOrder = 'desc';
					if (this.sortType === 'endTime') {
						this.sortOrder = 'asc';
					}

				}
				this.showSortDropdown = false;
			},
			displayednotStartedStatus(tasks){
				const now = dayjs().format('YYYY-MM-DD HH:mm:ss')
				tasks.forEach(task => {
				    // 检查任务的startTime是否存在于当前时间之后
				    if (task.startTime && dayjs(task.startTime).isAfter(now)) {
				      // 如果满足条件，将notStartedStatus设置为1
				      task.notStartedStatus = 1;
				    }
				  });
				  
				  // 返回更新后的tasks数组（如果需要的话）
				  return tasks;
			}


		},
		mounted() {
			this.fetchData();
		},
		created() {
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
		background-color: #F8F9FA;
		display: flex;
		flex-direction: column;
		margin-top: 40rpx;
	}

	.header {
		padding: 0rpx 32rpx;
		display: flex;
		justify-content: space-between;
		align-items: center;
		background-color: #FFFFFF;
		margin-bottom: 0px;
	}

	.title {
		font-size: 34px;
		font-weight: 600;
		color: #333333;
		padding: 5px auto;
	}

	.search-container {
		flex: 1;
		position: relative;
		display: flex;
		align-items: center;
		margin: 0 20rpx 0 60rpx;
	}

	.search-input {
		flex: 1;
		height: 60rpx;
		padding: 0 60rpx 0 20rpx;
		background-color: #f5f5f5;
		border-radius: 30rpx;
		font-size: 14px;
		color: #333;
		border: 1px solid #e0e0e0;
	}

	.search-clear {
		position: absolute;
		right: 20rpx;
		color: #999;
	}

	.header-right {
		display: flex;
		gap: 40rpx;
	}

	.date {
		padding: 20rpx 32rpx;
		font-size: 14px;
		color: #999999;
		background-color: #FFFFFF;
	}

	.category-container {
		background-color: #FFFFFF;
		display: flex;
		flex-direction: column;
		gap: 10px;
		margin: 0 20rpx;
		margin-bottom: 20rpx;
	}

	.category-row {
		display: flex;
		justify-content: flex-start;
		gap: 10px;
	}

	.category-item {
		position: relative;
		padding: 16rpx 32rpx;
		border-radius: 32rpx;
		background-color: #F5F6F7;
		font-size: 14px;
		color: #666666;
	}

	.category-item.active {
		background-color: #E8F3FF;
		color: #4080FF;
	}

	.category-count {
		position: absolute;
		top: -6rpx;
		right: -6rpx;
		min-width: 32rpx;
		height: 32rpx;
		padding: 0 6rpx;
		border-radius: 16rpx;
		background-color: #FF6B6B;
		color: #FFFFFF;
		font-size: 12px;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.task-list {
		flex: 1;
		overflow: hidden;
	}

	.swipe-container {
		position: relative;
		overflow: hidden;
		/* overflow: visible; */
	}

	.task-card {
		position: relative;
		display: flex;
		background-color: #FFFFFF;
		border-radius: 16rpx;
		padding: 32rpx;
		margin: 0 20rpx 20rpx 20rpx;
		box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.05);
		transition: transform 0.3s ease;
		overflow: hidden;
		width: 370px;
		border-left: 4px solid transparent;
	}

	.task-content {
		flex: 1;
		display: flex;
		align-items: flex-start;
		width: 100%;
	}

	.actions {
		width: 130px;
		position: absolute;
		top: 0;
		right: -180px;
		bottom: 0;
		display: flex;
		align-items: center;
		background-color: #FFFFFF;
		transition: transform 0.3s ease;
		margin: 0 20rpx 20rpx 20rpx;
		padding: 32rpx;
		border-bottom: none;
		border-radius: 16rpx;
		box-shadow: 6rpx 0 12rpx rgba(0, 0, 0, 0.05);
	}

	.edit-btn,
	.delete-btn {
		padding: 2px 14px;
		width: 100%;
		height: 60%;
		background-color: transparent;
		color: #555;
		font-size: 16px;
		font-weight: 500;
		text-align-last: justify;
		align-items: center;
	}

	.edit-btn {
		background-color: #a8e6cf;
		align-items: center;
		margin-right: 5px;
	}

	.delete-btn {
		background-color: #FF6B6B;
	}

	.priority-dot {
		width: 12rpx;
		height: 12rpx;
		border-radius: 50%;
		margin-right: 20rpx;
		margin-top: 12rpx;
	}

	.priority-high {
		background-color: #FF6B6B;
	}

	.priority-medium {
		background-color: #FFC107;
	}

	.priority-low {
		background-color: #4080FF;
	}

	.task-info {
		flex: 1;
		margin-right: 20rpx;
	}

	.task-title {
		font-size: 16px;
		color: #333333;
		margin-bottom: 12rpx;
		display: block;
		white-space: nowrap;
		overflow: hidden;
		text-overflow: ellipsis;
	}

	.task-description {
		font-size: 14px;
		color: #666666;
		margin-bottom: 16rpx;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 1;
		overflow: hidden;
		line-height: 1.4;
	}

	.task-meta {
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.category-tag {
		padding: 4rpx 16rpx;
		border-radius: 8rpx;
		font-size: 12px;
	}

	.work {
		background-color: #E8F3FF;
		color: #4080FF;
	}

	.health {
		background-color: #FFF3E0;
		color: #FF9800;
	}

	.study {
		background-color: #E8F5E9;
		color: #4CAF50;
	}

	.personal {
		background-color: #F3E5F5;
		color: #9C27B0;
	}

	.play {
		background-color: #FFEBEE;
		color: #F44336;
	}

	.task-time {
		font-size: 12px;
		color: #999999;
	}

	.add-button {
		position: fixed;
		right: 40rpx;
		bottom: 60rpx;
		width: 100rpx;
		height: 100rpx;
		border-radius: 50%;
		background-color: #4080FF;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 4rpx 16rpx rgba(64, 128, 255, 0.4);
	}

	.popup-content {
		background-color: #FFFFFF;
		border-radius: 32rpx 32rpx 0 0;
		padding: 32rpx;
	}

	.popup-title {
		text-align: center;
		font-size: 16px;
		color: #333333;
		margin-bottom: 32rpx;
	}

	.popup-options {
		display: flex;
		flex-direction: column;
		gap: 32rpx;
	}

	.popup-option {
		display: flex;
		align-items: center;
		gap: 20rpx;
		font-size: 16px;
		color: #333333;
	}

	.popup-cancel {
		margin-top: 32rpx;
		text-align: center;
		font-size: 16px;
		color: #999999;
	}

	.cursor-pointer {
		cursor: pointer;
	}

	.edit-modal {
		width: 80vw;
		max-width: 600px;
		background-color: #fff;
		border-radius: 16rpx;
		padding: 32rpx;
		box-sizing: border-box;
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

	.close-icon {
		color: #999;
	}

	.form-item {
		margin-bottom: 32rpx;
	}

	.form-label {
		display: block;
		margin-bottom: 8rpx;
		font-size: 16px;
		color: #666;
	}

	.form-input,
	.form-textarea,
	.picker {
		width: 90%;
		padding: 20rpx;
		border: 1px solid #eee;
		border-radius: 8rpx;
		font-size: 15px;
		background-color: #fff;
	}

	.readonly-value {
		padding: 10px;
		color: #666;
		background-color: #f5f5f5;
		border-radius: 4px;
	}

	.form-textarea {
		height: 100rpx;
	}

	.picker {
		color: #333;
	}

	.save-btn {
		margin-top: 32rpx;
		background-color: #4080FF;
		color: #fff;
		font-size: 16px;
		height: 88rpx;
		line-height: 88rpx;
		border-radius: 8rpx;
	}

	.save-btn:active {
		opacity: 0.8;
	}

	.sort-container {
		position: relative;
		cursor: pointer;
	}

	.sort-dropdown {
		position: absolute;
		right: 0;
		top: 100%;
		width: 160px;
		background-color: #fff;
		border-radius: 8px;
		box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
		z-index: 100;
		padding: 10px 0;
		margin-top: 8px;
	}

	.sort-option {
		padding: 10px 16px;
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.sort-option:hover {
		background-color: #f5f5f5;
	}

	.sort-indicator {
		width: 20px;
		height: 20px;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.expired-task {
		border-left-color: #FF6B6B;
		opacity: 0.9;
	}

	.near-expired-task {
		border-left-color: #FFC107;
	}

	.time-container {
		display: flex;
		align-items: center;
		gap: 6rpx;
	}
	
	.status-tag {
	  position: absolute;
	  right: 32rpx;
	  bottom: 32rpx;
	  padding: 4rpx 12rpx;
	  border-radius: 12rpx;
	  font-size: 12px;
	  color: #999;
	  background-color: #f5f5f5;
	}

</style>