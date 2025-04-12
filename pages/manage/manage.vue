<template>
	<view class="admin-container">
		<!-- 顶部导航栏 -->
		<view class="header">
			<view class="logo-area">
				<uni-icons type="calendar" size="24" color="#4A90E2"></uni-icons>
				<text class="logo-text">时间管理与习惯养成系统</text>
			</view>
			<view class="user-area">
				<text class="username">管理员</text>
				<view class="avatar">
					<image class="avatar-img"
						src="https://readdy.ai/api/search-image?query=Professional%20portrait%20of%20an%20Asian%20business%20person%2C%20neutral%20expression%2C%20business%20attire%2C%20high%20quality%2C%20realistic%2C%20professional%20lighting%2C%20studio%20background%2C%20centered%20composition%2C%20subject%20fills%2080%20percent%20of%20frame%2C%20isolated%20on%20white%20background&width=80&height=80&seq=avatar1&orientation=squarish"
						mode="aspectFill"></image>
				</view>
				<view class="logout-btn cursor-pointer" @click="handleLogout">
					<uni-icons type="poweroff" size="20" color="#999"></uni-icons>
					<text class="logout-text">退出</text>
				</view>
			</view>
		</view>
		<view class="main-content">
			<!-- 左侧导航菜单 -->
			<view class="sidebar">
				<view v-for="(item, index) in menuItems" :key="index"
					:class="['menu-item', 'cursor-pointer', { active: activeMenu === item.key }]"
					@click="handleMenuChange(item.key)">
					<uni-icons :type="item.icon" size="20"
						:color="activeMenu === item.key ? '#4A90E2' : '#666'"></uni-icons>
					<text class="menu-text">{{ item.name }}</text>
				</view>
			</view>
			<!-- 右侧内容区域 -->
			<view class="content-area">
				<!-- 用户管理模块 -->
				<view v-if="activeMenu === 'users'" class="module-container">
					<view class="module-header">
						<text class="module-title">用户管理</text>
					</view>
					<view class="search-bar">
						<view class="search-item">
							<text class="search-label">用户名：</text>
							<input class="search-input" type="text" placeholder="请输入用户名"
								v-model="userSearchParams.username" />
						</view>
						<view class="search-item">
							<text class="search-label">状态：</text>
							<view class="select-wrapper">
								<picker :range="statusOptions" @change="handleStatusChange">
									<view class="picker-view">
										{{ userSearchParams.status || '全部' }}
										<uni-icons type="down" size="14" color="#666"></uni-icons>
									</view>
								</picker>
							</view>
						</view>
						<view class="search-item">
							<text class="search-label">注册时间：</text>
							<picker mode="date" :value="userSearchParams.startDate" @change="handleStartDateChange">
								<view class="picker-view">
									{{ userSearchParams.startDate || '开始日期' }}
									<uni-icons type="calendar" size="14" color="#666"></uni-icons>
								</view>
							</picker>
							<text class="date-separator">至</text>
							<picker mode="date" :value="userSearchParams.endDate" @change="handleEndDateChange">
								<view class="picker-view">
									{{ userSearchParams.endDate || '结束日期' }}
									<uni-icons type="calendar" size="14" color="#666"></uni-icons>
								</view>
							</picker>
						</view>
						<view class="search-buttons">
							<button class="search-btn cursor-pointer" @click="searchUsers">搜索</button>
							<button class="reset-btn cursor-pointer" @click="resetUserSearch">重置</button>
						</view>
					</view>
					<view class="table-container">
						<view class="table-header">
							<view class="table-cell th-id">ID</view>
							<view class="table-cell th-username">用户名</view>
							<view class="table-cell th-email">邮箱</view>
							<view class="table-cell th-register">注册时间</view>
							<view class="table-cell th-login">最后登录</view>
							<view class="table-cell th-status">状态</view>
							<view class="table-cell th-action">操作</view>
						</view>
						<view v-for="(user, index) in userList" :key="user.id"
							:class="['table-row', index % 2 === 0 ? 'row-even' : 'row-odd']">
							<view class="table-cell td-id">{{ user.id }}</view>
							<view class="table-cell td-username">{{ user.username }}</view>
							<view class="table-cell td-email">{{ user.email }}</view>
							<view class="table-cell td-register">{{ user.registerTime }}</view>
							<view class="table-cell td-login">{{ user.lastLogin }}</view>
							<view class="table-cell td-status">
								<view
									:class="['status-badge', user.status === '活跃' ? 'status-active' : 'status-inactive']">
									{{ user.status }}
								</view>
							</view>
							<view class="table-cell td-action">
								<view class="action-buttons">
									<view class="edit-btn cursor-pointer" @click="editUser(user.id)">
										<uni-icons type="compose" size="16" color="#4A90E2"></uni-icons>
										<text>编辑</text>
									</view>
									<view
										:class="['status-btn', 'cursor-pointer', user.status === '活跃' ? 'disable-btn' : 'enable-btn']"
										@click="toggleUserStatus(user.id)">
										<uni-icons :type="user.status === '活跃' ? 'close' : 'checkmarkempty'" size="16"
											:color="user.status === '活跃' ? '#F56C6C' : '#67C23A'"></uni-icons>
										<text>{{ user.status === '活跃' ? '禁用' : '启用' }}</text>
									</view>
									<view class="delete-btn cursor-pointer" @click="confirmDeleteUser(user.id)">
										<uni-icons type="trash" size="16" color="#F56C6C"></uni-icons>
										<text>删除</text>
									</view>
								</view>
							</view>
						</view>
					</view>
					<view class="pagination">
						<view class="page-btn prev-btn cursor-pointer" @click="prevPage"
							:class="{ disabled: currentPage === 1 }">
							<uni-icons type="left" size="14" :color="currentPage === 1 ? '#ccc' : '#666'"></uni-icons>
							<text>上一页</text>
						</view>
						<view v-for="page in displayedPages" :key="page"
							:class="['page-number', 'cursor-pointer', { active: currentPage === page }]"
							@click="goToPage(page)">
							{{ page }}
						</view>
						<view class="page-btn next-btn cursor-pointer" @click="nextPage"
							:class="{ disabled: currentPage === totalPages }">
							<text>下一页</text>
							<uni-icons type="right" size="14"
								:color="currentPage === totalPages ? '#ccc' : '#666'"></uni-icons>
						</view>
					</view>
				</view>
				<!-- 系统日志模块 -->
				<view v-if="activeMenu === 'syslog'" class="module-container">
					<view class="module-header">
						<text class="module-title">系统操作日志</text>
					</view>
					<view class="search-bar">
						<view class="search-item">
							<text class="search-label">操作类型：</text>
							<view class="select-wrapper">
								<picker :range="operationTypes" @change="handleOperationTypeChange">
									<view class="picker-view">
										{{ sysLogSearchParams.operationType || '全部' }}
										<uni-icons type="down" size="14" color="#666"></uni-icons>
									</view>
								</picker>
							</view>
						</view>
						<view class="search-item">
							<text class="search-label">操作人：</text>
							<input class="search-input" type="text" placeholder="请输入操作人"
								v-model="sysLogSearchParams.operator" />
						</view>
						<view class="search-item">
							<text class="search-label">操作时间：</text>
							<picker mode="date" :value="sysLogSearchParams.startDate"
								@change="handleSysLogStartDateChange">
								<view class="picker-view">
									{{ sysLogSearchParams.startDate || '开始日期' }}
									<uni-icons type="calendar" size="14" color="#666"></uni-icons>
								</view>
							</picker>
							<text class="date-separator">至</text>
							<picker mode="date" :value="sysLogSearchParams.endDate" @change="handleSysLogEndDateChange">
								<view class="picker-view">
									{{ sysLogSearchParams.endDate || '结束日期' }}
									<uni-icons type="calendar" size="14" color="#666"></uni-icons>
								</view>
							</picker>
						</view>
						<view class="search-buttons">
							<button class="search-btn cursor-pointer" @click="searchSysLogs">搜索</button>
							<button class="reset-btn cursor-pointer" @click="resetSysLogSearch">重置</button>
						</view>
					</view>
					<view class="table-container">
						<view class="table-header">
							<view class="table-cell th-id">ID</view>
							<view class="table-cell th-operation">操作类型</view>
							<view class="table-cell th-operator">操作人</view>
							<view class="table-cell th-ip">IP地址</view>
							<view class="table-cell th-time">操作时间</view>
							<view class="table-cell th-status">状态</view>
							<view class="table-cell th-detail">详情</view>
						</view>
						<view v-for="(log, index) in sysLogList" :key="log.id"
							:class="['table-row', index % 2 === 0 ? 'row-even' : 'row-odd']">
							<view class="table-cell td-id">{{ log.id }}</view>
							<view class="table-cell td-operation">
								<view :class="['operation-badge', getOperationClass(log.operationType)]">
									{{ log.operationType }}
								</view>
							</view>
							<view class="table-cell td-operator">{{ log.operator }}</view>
							<view class="table-cell td-ip">{{ log.ip }}</view>
							<view class="table-cell td-time">{{ log.operationTime }}</view>
							<view class="table-cell td-status">
								<view
									:class="['status-badge', log.status === '成功' ? 'status-success' : 'status-failed']">
									{{ log.status }}
								</view>
							</view>
							<view class="table-cell td-detail">
								<view class="detail-btn cursor-pointer" @click="viewLogDetail(log.id)">
									<uni-icons type="eye" size="16" color="#4A90E2"></uni-icons>
									<text>查看</text>
								</view>
							</view>
						</view>
					</view>
					<view class="pagination">
						<view class="page-btn prev-btn cursor-pointer" @click="prevSysLogPage"
							:class="{ disabled: sysLogCurrentPage === 1 }">
							<uni-icons type="left" size="14"
								:color="sysLogCurrentPage === 1 ? '#ccc' : '#666'"></uni-icons>
							<text>上一页</text>
						</view>
						<view v-for="page in sysLogDisplayedPages" :key="page"
							:class="['page-number', 'cursor-pointer', { active: sysLogCurrentPage === page }]"
							@click="goToSysLogPage(page)">
							{{ page }}
						</view>
						<view class="page-btn next-btn cursor-pointer" @click="nextSysLogPage"
							:class="{ disabled: sysLogCurrentPage === sysLogTotalPages }">
							<text>下一页</text>
							<uni-icons type="right" size="14"
								:color="sysLogCurrentPage === sysLogTotalPages ? '#ccc' : '#666'"></uni-icons>
						</view>
					</view>
				</view>
				<!-- 登录日志模块 -->
				<view v-if="activeMenu === 'loginlog'" class="module-container">
					<view class="module-header">
						<text class="module-title">用户登录日志</text>
					</view>
					<view class="search-bar">
						<view class="search-item">
							<text class="search-label">用户名：</text>
							<input class="search-input" type="text" placeholder="请输入用户名"
								v-model="loginLogSearchParams.username" />
						</view>
						<view class="search-item">
							<text class="search-label">IP地址：</text>
							<input class="search-input" type="text" placeholder="请输入IP地址"
								v-model="loginLogSearchParams.ip" />
						</view>
						<view class="search-item">
							<text class="search-label">登录状态：</text>
							<view class="select-wrapper">
								<picker :range="loginStatusOptions" @change="handleLoginStatusChange">
									<view class="picker-view">
										{{ loginLogSearchParams.status || '全部' }}
										<uni-icons type="down" size="14" color="#666"></uni-icons>
									</view>
								</picker>
							</view>
						</view>
						<view class="search-item">
							<text class="search-label">登录时间：</text>
							<picker mode="date" :value="loginLogSearchParams.startDate"
								@change="handleLoginLogStartDateChange">
								<view class="picker-view">
									{{ loginLogSearchParams.startDate || '开始日期' }}
									<uni-icons type="calendar" size="14" color="#666"></uni-icons>
								</view>
							</picker>
							<text class="date-separator">至</text>
							<picker mode="date" :value="loginLogSearchParams.endDate"
								@change="handleLoginLogEndDateChange">
								<view class="picker-view">
									{{ loginLogSearchParams.endDate || '结束日期' }}
									<uni-icons type="calendar" size="14" color="#666"></uni-icons>
								</view>
							</picker>
						</view>
						<view class="search-buttons">
							<button class="search-btn cursor-pointer" @click="searchLoginLogs">搜索</button>
							<button class="reset-btn cursor-pointer" @click="resetLoginLogSearch">重置</button>
						</view>
					</view>
					<view class="table-container">
						<view class="table-header">
							<view class="table-cell th-id">ID</view>
							<view class="table-cell th-username">用户名</view>
							<view class="table-cell th-ip">IP地址</view>
							<view class="table-cell th-device">设备信息</view>
							<view class="table-cell th-time">登录时间</view>
							<view class="table-cell th-status">状态</view>
							<view class="table-cell th-message">消息</view>
						</view>
						<view v-for="(log, index) in loginLogList" :key="log.id"
							:class="['table-row', index % 2 === 0 ? 'row-even' : 'row-odd']">
							<view class="table-cell td-id">{{ log.id }}</view>
							<view class="table-cell td-username">{{ log.username }}</view>
							<view class="table-cell td-ip">{{ log.ip }}</view>
							<view class="table-cell td-device">{{ log.device }}</view>
							<view class="table-cell td-time">{{ log.loginTime }}</view>
							<view class="table-cell td-status">
								<view
									:class="['status-badge', log.status === '成功' ? 'status-success' : 'status-failed']">
									{{ log.status }}
								</view>
							</view>
							<view class="table-cell td-message">{{ log.message }}</view>
						</view>
					</view>
					<view class="pagination">
						<view class="page-btn prev-btn cursor-pointer" @click="prevLoginLogPage"
							:class="{ disabled: loginLogCurrentPage === 1 }">
							<uni-icons type="left" size="14"
								:color="loginLogCurrentPage === 1 ? '#ccc' : '#666'"></uni-icons>
							<text>上一页</text>
						</view>
						<view v-for="page in loginLogDisplayedPages" :key="page"
							:class="['page-number', 'cursor-pointer', { active: loginLogCurrentPage === page }]"
							@click="goToLoginLogPage(page)">
							{{ page }}
						</view>
						<view class="page-btn next-btn cursor-pointer" @click="nextLoginLogPage"
							:class="{ disabled: loginLogCurrentPage === loginLogTotalPages }">
							<text>下一页</text>
							<uni-icons type="right" size="14"
								:color="loginLogCurrentPage === loginLogTotalPages ? '#ccc' : '#666'"></uni-icons>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>
<script>
	export default {
		data() {
			return {
				// 菜单项数据
				menuItems: [{
						key: 'users',
						name: '用户管理',
						icon: 'person'
					},
					{
						key: 'syslog',
						name: '系统日志',
						icon: 'bars'
					},
					{
						key: 'loginlog',
						name: '登录日志',
						icon: 'locked'
					}
				],
				// 当前激活的菜单
				activeMenu: 'users',
				// 用户管理相关数据
				userSearchParams: {
					username: '',
					status: '',
					startDate: '',
					endDate: ''
				},
				statusOptions: ['全部', '活跃', '禁用'],
				userList: [{
						id: 1,
						username: '张三',
						email: 'zhangsan@example.com',
						registerTime: '2025-03-15 10:23:45',
						lastLogin: '2025-04-12 08:30:12',
						status: '活跃'
					},
					{
						id: 2,
						username: '李四',
						email: 'lisi@example.com',
						registerTime: '2025-03-18 14:35:22',
						lastLogin: '2025-04-11 16:45:30',
						status: '活跃'
					},
					{
						id: 3,
						username: '王五',
						email: 'wangwu@example.com',
						registerTime: '2025-03-20 09:12:38',
						lastLogin: '2025-04-10 11:20:45',
						status: '禁用'
					},
					{
						id: 4,
						username: '赵六',
						email: 'zhaoliu@example.com',
						registerTime: '2025-03-22 16:40:15',
						lastLogin: '2025-04-08 14:15:22',
						status: '活跃'
					},
					{
						id: 5,
						username: '钱七',
						email: 'qianqi@example.com',
						registerTime: '2025-03-25 11:28:33',
						lastLogin: '2025-04-05 09:50:18',
						status: '活跃'
					},
					{
						id: 6,
						username: '孙八',
						email: 'sunba@example.com',
						registerTime: '2025-03-28 15:05:42',
						lastLogin: '2025-04-02 17:30:11',
						status: '禁用'
					},
					{
						id: 7,
						username: '周九',
						email: 'zhoujiu@example.com',
						registerTime: '2025-03-30 08:55:19',
						lastLogin: '2025-03-31 10:40:25',
						status: '活跃'
					},
					{
						id: 8,
						username: '吴十',
						email: 'wushi@example.com',
						registerTime: '2025-04-01 13:22:07',
						lastLogin: '2025-04-12 09:15:33',
						status: '活跃'
					}
				],
				// 系统日志相关数据
				sysLogSearchParams: {
					operationType: '',
					operator: '',
					startDate: '',
					endDate: ''
				},
				operationTypes: ['全部', '新增', '修改', '删除', '查询', '导出', '导入'],
				sysLogList: [{
						id: 1,
						operationType: '新增',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-12 09:30:45',
						status: '成功',
						detail: '新增用户：张三'
					},
					{
						id: 2,
						operationType: '修改',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-12 10:15:22',
						status: '成功',
						detail: '修改用户信息：李四'
					},
					{
						id: 3,
						operationType: '删除',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-11 16:40:18',
						status: '成功',
						detail: '删除用户：王五'
					},
					{
						id: 4,
						operationType: '查询',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-11 14:22:33',
						status: '成功',
						detail: '查询用户列表'
					},
					{
						id: 5,
						operationType: '导出',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-10 11:05:47',
						status: '成功',
						detail: '导出用户数据'
					},
					{
						id: 6,
						operationType: '导入',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-10 09:18:29',
						status: '失败',
						detail: '导入用户数据失败：格式错误'
					},
					{
						id: 7,
						operationType: '修改',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-09 15:30:12',
						status: '成功',
						detail: '修改系统配置'
					},
					{
						id: 8,
						operationType: '新增',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-09 13:45:38',
						status: '成功',
						detail: '新增角色：运营'
					}
				],
				// 登录日志相关数据
				loginLogSearchParams: {
					username: '',
					ip: '',
					status: '',
					startDate: '',
					endDate: ''
				},
				loginStatusOptions: ['全部', '成功', '失败'],
				loginLogList: [{
						id: 1,
						username: '张三',
						ip: '192.168.1.101',
						device: 'Chrome 95.0 / Windows 10',
						loginTime: '2025-04-12 08:30:12',
						status: '成功',
						message: '登录成功'
					},
					{
						id: 2,
						username: '李四',
						ip: '192.168.1.102',
						device: 'Firefox 94.0 / macOS',
						loginTime: '2025-04-11 16:45:30',
						status: '成功',
						message: '登录成功'
					},
					{
						id: 3,
						username: '王五',
						ip: '192.168.1.103',
						device: 'Safari 15.0 / iOS 15',
						loginTime: '2025-04-11 14:20:18',
						status: '失败',
						message: '密码错误'
					},
					{
						id: 4,
						username: '赵六',
						ip: '192.168.1.104',
						device: 'Edge 96.0 / Windows 11',
						loginTime: '2025-04-10 09:15:42',
						status: '成功',
						message: '登录成功'
					},
					{
						id: 5,
						username: '钱七',
						ip: '192.168.1.105',
						device: 'Chrome 95.0 / Android 12',
						loginTime: '2025-04-09 17:30:25',
						status: '成功',
						message: '登录成功'
					},
					{
						id: 6,
						username: '孙八',
						ip: '192.168.1.106',
						device: 'Firefox 94.0 / Ubuntu',
						loginTime: '2025-04-09 11:45:33',
						status: '失败',
						message: '账号已锁定'
					},
					{
						id: 7,
						username: '周九',
						ip: '192.168.1.107',
						device: 'Chrome 95.0 / Windows 10',
						loginTime: '2025-04-08 15:20:19',
						status: '成功',
						message: '登录成功'
					},
					{
						id: 8,
						username: '吴十',
						ip: '192.168.1.108',
						device: 'Safari 15.0 / macOS',
						loginTime: '2025-04-08 10:35:47',
						status: '成功',
						message: '登录成功'
					}
				],
				// 分页相关数据
				currentPage: 1,
				pageSize: 8,
				totalPages: 5,
				sysLogCurrentPage: 1,
				sysLogPageSize: 8,
				sysLogTotalPages: 4,
				loginLogCurrentPage: 1,
				loginLogPageSize: 8,
				loginLogTotalPages: 3
			}
		},
		computed: {
			displayedPages() {
				const pages = [];
				const maxVisiblePages = 5;
				if (this.totalPages <= maxVisiblePages) {
					for (let i = 1; i <= this.totalPages; i++) {
						pages.push(i);
					}
				} else {
					if (this.currentPage <= 3) {
						for (let i = 1; i <= 5; i++) {
							pages.push(i);
						}
					} else if (this.currentPage >= this.totalPages - 2) {
						for (let i = this.totalPages - 4; i <= this.totalPages; i++) {
							pages.push(i);
						}
					} else {
						for (let i = this.currentPage - 2; i <= this.currentPage + 2; i++) {
							pages.push(i);
						}
					}
				}
				return pages;
			},
			sysLogDisplayedPages() {
				const pages = [];
				const maxVisiblePages = 5;
				if (this.sysLogTotalPages <= maxVisiblePages) {
					for (let i = 1; i <= this.sysLogTotalPages; i++) {
						pages.push(i);
					}
				} else {
					if (this.sysLogCurrentPage <= 3) {
						for (let i = 1; i <= 5; i++) {
							pages.push(i);
						}
					} else if (this.sysLogCurrentPage >= this.sysLogTotalPages - 2) {
						for (let i = this.sysLogTotalPages - 4; i <= this.sysLogTotalPages; i++) {
							pages.push(i);
						}
					} else {
						for (let i = this.sysLogCurrentPage - 2; i <= this.sysLogCurrentPage + 2; i++) {
							pages.push(i);
						}
					}
				}
				return pages;
			},
			loginLogDisplayedPages() {
				const pages = [];
				const maxVisiblePages = 5;
				if (this.loginLogTotalPages <= maxVisiblePages) {
					for (let i = 1; i <= this.loginLogTotalPages; i++) {
						pages.push(i);
					}
				} else {
					if (this.loginLogCurrentPage <= 3) {
						for (let i = 1; i <= 5; i++) {
							pages.push(i);
						}
					} else if (this.loginLogCurrentPage >= this.loginLogTotalPages - 2) {
						for (let i = this.loginLogTotalPages - 4; i <= this.loginLogTotalPages; i++) {
							pages.push(i);
						}
					} else {
						for (let i = this.loginLogCurrentPage - 2; i <= this.loginLogCurrentPage + 2; i++) {
							pages.push(i);
						}
					}
				}
				return pages;
			}
		},
		methods: {
			// 菜单切换
			handleMenuChange(key) {
				this.activeMenu = key;
			},
			// 用户管理相关方法
			handleStatusChange(e) {
				const index = e.detail.value;
				this.userSearchParams.status = this.statusOptions[index] === '全部' ? '' : this.statusOptions[index];
			},
			handleStartDateChange(e) {
				this.userSearchParams.startDate = e.detail.value;
			},
			handleEndDateChange(e) {
				this.userSearchParams.endDate = e.detail.value;
			},
			searchUsers() {
				// 模拟搜索逻辑
				const filteredUsers = this.userList.filter(user => {
					const matchUsername = !this.userSearchParams.username ||
						user.username.toLowerCase().includes(this.userSearchParams.username.toLowerCase());
					const matchStatus = !this.userSearchParams.status ||
						user.status === this.userSearchParams.status;
					const matchStartDate = !this.userSearchParams.startDate ||
						new Date(user.registerTime) >= new Date(this.userSearchParams.startDate);
					const matchEndDate = !this.userSearchParams.endDate ||
						new Date(user.registerTime) <= newDate(this.userSearchParams.endDate + ' 23:59:59');

					return matchUsername && matchStatus && matchStartDate && matchEndDate;
				});

				this.userList = filteredUsers;
				if (filteredUsers.length === 0) {
					uni.showToast({
						title: '未找到匹配的用户',
						icon: 'none',
						duration: 2000
					});
				} else {
					uni.showToast({
						title: `找到 ${filteredUsers.length} 个用户`,
						icon: 'success',
						duration: 2000
					});
				}
				this.currentPage = 1;
			},
			resetUserSearch() {
				this.userSearchParams = {
					username: '',
					status: '',
					startDate: '',
					endDate: ''
				};
				// 重置用户列表数据
				this.userList = [{
						id: 1,
						username: '张三',
						email: 'zhangsan@example.com',
						registerTime: '2025-03-15 10:23:45',
						lastLogin: '2025-04-12 08:30:12',
						status: '活跃'
					},
					{
						id: 2,
						username: '李四',
						email: 'lisi@example.com',
						registerTime: '2025-03-18 14:35:22',
						lastLogin: '2025-04-11 16:45:30',
						status: '活跃'
					},
					{
						id: 3,
						username: '王五',
						email: 'wangwu@example.com',
						registerTime: '2025-03-20 09:12:38',
						lastLogin: '2025-04-10 11:20:45',
						status: '禁用'
					},
					{
						id: 4,
						username: '赵六',
						email: 'zhaoliu@example.com',
						registerTime: '2025-03-22 16:40:15',
						lastLogin: '2025-04-08 14:15:22',
						status: '活跃'
					},
					{
						id: 5,
						username: '钱七',
						email: 'qianqi@example.com',
						registerTime: '2025-03-25 11:28:33',
						lastLogin: '2025-04-05 09:50:18',
						status: '活跃'
					},
					{
						id: 6,
						username: '孙八',
						email: 'sunba@example.com',
						registerTime: '2025-03-28 15:05:42',
						lastLogin: '2025-04-02 17:30:11',
						status: '禁用'
					},
					{
						id: 7,
						username: '周九',
						email: 'zhoujiu@example.com',
						registerTime: '2025-03-30 08:55:19',
						lastLogin: '2025-03-31 10:40:25',
						status: '活跃'
					},
					{
						id: 8,
						username: '吴十',
						email: 'wushi@example.com',
						registerTime: '2025-04-01 13:22:07',
						lastLogin: '2025-04-12 09:15:33',
						status: '活跃'
					}
				];
				this.currentPage = 1;
				uni.showToast({
					title: '已重置搜索条件',
					icon: 'success',
					duration: 2000
				});
			},
			editUser(id) {
				console.log('编辑用户', id);
			},
			toggleUserStatus(id) {
				const user = this.userList.find(u => u.id === id);
				if (user) {
					user.status = user.status === '活跃' ? '禁用' : '活跃';
				}
			},
			confirmDeleteUser(id) {
				uni.showModal({
					title: '确认删除',
					content: '确定要删除该用户吗？此操作不可恢复。',
					confirmText: '确认',
					confirmColor: '#F56C6C',
					cancelText: '取消',
					success: (res) => {
						if (res.confirm) {
							this.deleteUser(id);
						}
					}
				});
			},
			deleteUser(id) {
				const index = this.userList.findIndex(u => u.id === id);
				if (index !== -1) {
					this.userList.splice(index, 1);
					uni.showToast({
						title: '删除成功',
						icon: 'success',
						duration: 2000
					});
				}
			},
			// 分页相关方法
			prevPage() {
				if (this.currentPage > 1) {
					this.currentPage--;
				}
			},
			nextPage() {
				if (this.currentPage < this.totalPages) {
					this.currentPage++;
				}
			},
			goToPage(page) {
				this.currentPage = page;
			},
			// 系统日志相关方法
			handleOperationTypeChange(e) {
				const index = e.detail.value;
				this.sysLogSearchParams.operationType = this.operationTypes[index] === '全部' ? '' : this.operationTypes[
					index];
			},
			handleSysLogStartDateChange(e) {
				this.sysLogSearchParams.startDate = e.detail.value;
			},
			handleSysLogEndDateChange(e) {
				this.sysLogSearchParams.endDate = e.detail.value;
			},
			searchSysLogs() {
				// 模拟搜索逻辑
				const filteredLogs = this.sysLogList.filter(log => {
					const matchOperationType = !this.sysLogSearchParams.operationType ||
						log.operationType === this.sysLogSearchParams.operationType;
					const matchOperator = !this.sysLogSearchParams.operator ||
						log.operator.toLowerCase().includes(this.sysLogSearchParams.operator.toLowerCase());
					const matchStartDate = !this.sysLogSearchParams.startDate ||
						new Date(log.operationTime) >= new Date(this.sysLogSearchParams.startDate);
					const matchEndDate = !this.sysLogSearchParams.endDate ||
						new Date(log.operationTime) <= new Date(this.sysLogSearchParams.endDate + ' 23:59:59');

					return matchOperationType && matchOperator && matchStartDate && matchEndDate;
				});

				this.sysLogList = filteredLogs;
				if (filteredLogs.length === 0) {
					uni.showToast({
						title: '未找到匹配的日志',
						icon: 'none',
						duration: 2000
					});
				} else {
					uni.showToast({
						title: `找到 ${filteredLogs.length} 条日志`,
						icon: 'success',
						duration: 2000
					});
				}
				this.sysLogCurrentPage = 1;
			},
			resetSysLogSearch() {
				this.sysLogSearchParams = {
					operationType: '',
					operator: '',
					startDate: '',
					endDate: ''
				};
				// 重置系统日志数据
				this.sysLogList = [{
						id: 1,
						operationType: '新增',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-12 09:30:45',
						status: '成功',
						detail: '新增用户：张三'
					},
					{
						id: 2,
						operationType: '修改',
						operator: '管理员',
						ip: '192.168.2',
						operationType: '修改',
						operator: '管理员 ',
						ip: '192.168.1.100',
						operationTime: '2025-04 - 12:10: 15: 22 ',
						status: '成功',
						detail: '修改用户信息： 李四 ',
					},
					{
						id: 3,
						operationType: '删除',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-11 16:40:18',
						status: '成功',
						detail: '删除用户：王五'
					},
					{
						id: 4,
						operationType: '查询',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-11 14:22:33',
						status: '成功',
						detail: '查询用户列表'
					},
					{
						id: 5,
						operationType: '导出',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-10 11:05:47',
						status: '成功',
						detail: '导出用户数据'
					},
					{
						id: 6,
						operationType: '导入',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-10 09:18:29',
						status: '失败',
						detail: '导入用户数据失败：格式错误'
					},
					{
						id: 7,
						operationType: '修改',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-09 15:30:12',
						status: '成功',
						detail: '修改系统配置'
					},
					{
						id: 8,
						operationType: '新增',
						operator: '管理员',
						ip: '192.168.1.100',
						operationTime: '2025-04-09 13:45:38',
						status: '成功',
						detail: '新增角色：运营'
					}
				];
				this.sysLogCurrentPage = 1;
				uni.showToast({
					title: '已重置搜索条件',
					icon: 'success',
					duration: 2000
				});
			},
			viewLogDetail(id) {
				console.log('查看日志详情', id);
			},
			getOperationClass(type) {
				switch (type) {
					case '新增':
						return 'operation-add';
					case '修改':
						return 'operation-edit';
					case '删除':
						return 'operation-delete';
					case '查询':
						return 'operation-query';
					case '导出':
						return 'operation-export';
					case '导入':
						return 'operation-import';
					default:
						return '';
				}
			},
			prevSysLogPage() {
				if (this.sysLogCurrentPage > 1) {
					this.sysLogCurrentPage--;
				}
			},
			nextSysLogPage() {
				if (this.sysLogCurrentPage < this.sysLogTotalPages) {
					this.sysLogCurrentPage++;
				}
			},
			goToSysLogPage(page) {
				this.sysLogCurrentPage = page;
			},
			// 登录日志相关方法
			handleLoginStatusChange(e) {
				const index = e.detail.value;
				this.loginLogSearchParams.status = this.loginStatusOptions[index] === '全部' ? '' : this
					.loginStatusOptions[index];
			},
			handleLoginLogStartDateChange(e) {
				this.loginLogSearchParams.startDate = e.detail.value;
			},
			handleLoginLogEndDateChange(e) {
				this.loginLogSearchParams.endDate = e.detail.value;
			},
			searchLoginLogs() {
				// 模拟搜索逻辑
				const filteredLogs = this.loginLogList.filter(log => {
					const matchUsername = !this.loginLogSearchParams.username ||
						log.username.toLowerCase().includes(this.loginLogSearchParams.username
							.toLowerCase());
					const matchIp = !this.loginLogSearchParams.ip ||
						log.ip.includes(this.loginLogSearchParams.ip);
					const matchStatus = !this.loginLogSearchParams.status ||
						log.status === this.loginLogSearchParams.status;
					const matchStartDate = !this.loginLogSearchParams.startDate ||
						new Date(log.loginTime) >= new Date(this.loginLogSearchParams.startDate);
					const matchEndDate = !this.loginLogSearchParams.endDate ||
						new Date(log.loginTime) <= new Date(this.loginLogSearchParams.endDate +
							' 23:59:59');

					return matchUsername && matchIp && matchStatus && matchStartDate && matchEndDate;
				});

				this.loginLogList = filteredLogs;
				if (filteredLogs.length === 0) {
					uni.showToast({
						title: '未找到匹配的登录日志',
						icon: 'none',
						duration: 2000
					});
				} else {
					uni.showToast({
						title: `找到 ${filteredLogs.length} 条登录日志`,
						icon: 'success',
						duration: 2000
					});
				}
				this.loginLogCurrentPage = 1;
			},
			resetLoginLogSearch() {
				this.loginLogSearchParams = {
					username: '',
					ip: '',
					status: '',
					startDate: '',
					endDate: ''
				};
				// 重置登录日志数据
				this.loginLogList = [{
						id: 1,
						username: '张三',
						ip: '192.168.1.101',
						device: 'Chrome 95.0 / Windows 10',
						loginTime: '2025-04-12 08:30:12',
						status: '成功',
						message: '登录成功'
					},
					{
						id: 2,
						username: '李四',
						ip: '192.168.1.102',
						device: 'Firefox 94.0 / macOS',
						loginTime: '2025-04-11 16:45:30',
						status: '成功',
						message: '登录成功'
					},
					{
						id: 3,
						username: '王五',
						ip: '192.168.1.103',
						device: 'Safari 15.0 / iOS 15',
						loginTime: '2025-04-11 14:20:18',
						status: '失败',
						message: '密码错误'
					},
					{
						id: 4,
						username: '赵六',
						ip: '192.168.1.104',
						device: 'Edge 96.0 / Windows 11',
						loginTime: '2025-04-10 09:15:42',
						status: '成功',
						message: '登录成功'
					},
					{
						id: 5,
						username: '钱七',
						ip: '192.168.1.105',
						device: 'Chrome 95.0 / Android 12',
						loginTime: '2025-04-09 17:30:25',
						status: '成功',
						message: '登录成功'
					},
					{
						id: 6,
						username: '孙八',
						ip: '192.168.1.106',
						device: 'Firefox 94.0 / Ubuntu',
						loginTime: '2025-04-09 11:45:33',
						status: '失败',
						message: '账号已锁定'
					},
					{
						id: 7,
						username: '周九',
						ip: '192.168.1.107',
						device: 'Chrome 95.0 / Windows 10',
						loginTime: '2025-04-08 15:20:19',
						status: '成功',
						message: '登录成功'
					},
					{
						id: 8,
						username: '吴十',
						ip: '192.168.1.108',
						device: 'Safari 15.0 / macOS',
						loginTime: '2025-04-08 10:35:47',
						status: '成功',
						message: '登录成功'
					}
				];
				this.loginLogCurrentPage = 1;
				uni.showToast({
					title: '已重置搜索条件',
					icon: 'success',
					duration: 2000
				});
			},
			prevLoginLogPage() {
				if (this.loginLogCurrentPage > 1) {
					this.loginLogCurrentPage--;
				}
			},
			nextLoginLogPage() {
				if (this.loginLogCurrentPage < this.loginLogTotalPages) {
					this.loginLogCurrentPage++;
				}
			},
			goToLoginLogPage(page) {
				this.loginLogCurrentPage = page;
			},
			// 退出登录
			handleLogout() {
				console.log('退出登录');
			}
		},
	}
</script>

<style>
	page {
		height: 100%;
	}

	.cursor-pointer {
		cursor: pointer;
	}

	.admin-container {
		display: flex;
		flex-direction: column;
		height: 100%;
		background-color: #F5F7FA;
		font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
	}

	/* 顶部导航栏 */
	.header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		height: 120rpx;
		padding: 0 40rpx;
		background-color: #FFFFFF;
		box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
		flex-shrink: 0;
	}

	.logo-area {
		display: flex;
		align-items: center;
	}

	.logo-text {
		margin-left: 20rpx;
		font-size: 36px;
		font-weight: 600;
		color: #333333;
	}

	.user-area {
		display: flex;
		align-items: center;
	}

	.username {
		font-size: 28px;
		color: #333333;
		margin-right: 20rpx;
	}

	.avatar {
		width: 80rpx;
		height: 80rpx;
		border-radius: 50%;
		overflow: hidden;
		margin-right: 30rpx;
	}

	.avatar-img {
		width: 100%;
		height: 100%;
	}

	.logout-btn {
		display: flex;
		align-items: center;
		padding: 10rpx 20rpx;
		border-radius: 8rpx;
		background-color: #F5F7FA;
		transition: background-color 0.3s;
	}

	.logout-btn:hover {
		background-color: #E6F3FF;
	}

	.logout-text {
		margin-left: 10rpx;
		font-size: 28px;
		color: #666666;
	}

	/* 主内容区域 */
	.main-content {
		display: flex;
		flex: 1;
		height: 0;
		overflow: hidden;
	}

	/* 左侧导航菜单 */
	.sidebar {
		width: 240rpx;
		background-color: #FFFFFF;
		box-shadow: 2px 0 10px rgba(0, 0, 0, 0.05);
		padding: 40rpx 0;
		flex-shrink: 0;
	}

	.menu-item {
		display: flex;
		align-items: center;
		padding: 30rpx 40rpx;
		margin-bottom: 10rpx;
		transition: all 0.3s;
	}

	.menu-item:hover {
		background-color: #F5F7FA;
	}

	.menu-item.active {
		background-color: #E6F3FF;
		border-left: 8rpx solid #4A90E2;
	}

	.menu-text {
		margin-left: 20rpx;
		font-size: 28px;
		color: #666666;
	}

	.menu-item.active .menu-text {
		color: #4A90E2;
		font-weight: 500;
	}

	/* 右侧内容区域 */
	.content-area {
		flex: 1;
		padding: 40rpx;
		overflow-y: auto;
	}

	.module-container {
		background-color: #FFFFFF;
		border-radius: 8rpx;
		box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
		padding: 40rpx;
	}

	.module-header {
		margin-bottom: 40rpx;
	}

	.module-title {
		font-size: 36px;
		font-weight: 600;
		color: #333333;
	}

	/* 搜索栏 */
	.search-bar {
		display: flex;
		flex-wrap: wrap;
		margin-bottom: 40rpx;
		padding: 30rpx;
		background-color: #F5F7FA;
		border-radius: 8rpx;
	}

	.search-item {
		display: flex;
		align-items: center;
		margin-right: 40rpx;
		margin-bottom: 20rpx;
	}

	.search-label {
		font-size: 28px;
		color: #666666;
		margin-right: 20rpx;
		white-space: nowrap;
	}

	.search-input {
		width: 300rpx;
		height: 70rpx;
		padding: 0 20rpx;
		border: 2rpx solid #DCDFE6;
		border-radius: 8rpx;
		background-color: #FFFFFF;
		font-size: 28px;
		color: #333333;
	}

	.select-wrapper {
		width: 200rpx;
		height: 70rpx;
		border: 2rpx solid #DCDFE6;
		border-radius: 8rpx;
		background-color: #FFFFFF;
		position: relative;
	}

	.picker-view {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 20rpx;
		height: 100%;
		font-size: 28px;
		color: #333333;
	}

	.date-separator {
		margin: 0 10rpx;
		font-size: 28px;
		color: #666666;
	}

	.search-buttons {
		display: flex;
		align-items: center;
		margin-left: auto;
	}

	.search-btn {
		height: 70rpx;
		line-height: 70rpx;
		padding: 0 30rpx;
		background-color: #4A90E2;
		color: #FFFFFF;
		font-size: 28px;
		border-radius: 8rpx;
		margin-right: 20rpx;
		border: none;
	}

	.reset-btn {
		height: 70rpx;
		line-height: 70rpx;
		padding: 0 30rpx;
		background-color: #FFFFFF;
		color: #666666;
		font-size: 28px;
		border-radius: 8rpx;
		border: 2rpx solid #DCDFE6;
	}

	/* 表格样式 */
	.table-container {
		margin-bottom: 40rpx;
		border: 2rpx solid #EBEEF5;
		border-radius: 8rpx;
		overflow: hidden;
	}

	.table-header {
		display: flex;
		background-color: #F5F7FA;
		font-weight: 500;
		color: #333333;
	}

	.table-row {
		display: flex;
		border-top: 2rpx solid #EBEEF5;
	}

	.row-even {
		background-color: #FFFFFF;
	}

	.row-odd {
		background-color: #FAFAFA;
	}

	.table-cell {
		padding: 24rpx 20rpx;
		font-size: 28px;
		color: #606266;
		display: flex;
		align-items: center;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	/* 用户管理表格列宽 */
	.th-id,
	.td-id {
		width: 80rpx;
		flex-shrink: 0;
	}

	.th-username,
	.td-username {
		width: 160rpx;
		flex-shrink: 0;
	}

	.th-email,
	.td-email {
		width: 300rpx;
		flex-shrink: 0;
	}

	.th-register,
	.td-register,
	.th-login,
	.td-login {
		width: 240rpx;
		flex-shrink: 0;
	}

	.th-status,
	.td-status {
		width: 120rpx;
		flex-shrink: 0;
	}

	.th-action,
	.td-action {
		flex: 1;
	}

	/* 系统日志表格列宽 */
	.th-operation,
	.td-operation {
		width: 120rpx;
		flex-shrink: 0;
	}

	.th-operator,
	.td-operator {
		width: 160rpx;
		flex-shrink: 0;
	}

	.th-ip,
	.td-ip {
		width: 200rpx;
		flex-shrink: 0;
	}

	.th-time,
	.td-time {
		width: 240rpx;
		flex-shrink: 0;
	}

	.th-detail,
	.td-detail {
		flex: 1;
	}

	/* 登录日志表格列宽 */
	.th-device,
	.td-device {
		width: 300rpx;
		flex-shrink: 0;
	}

	.th-message,
	.td-message {
		flex: 1;
	}

	/* 状态标签 */
	.status-badge {
		padding: 4rpx 16rpx;
		border-radius: 20rpx;
		font-size: 24px;
		text-align: center;
	}

	.status-active {
		background-color: #E8F6F1;
		color: #67C23A;
	}

	.status-inactive {
		background-color: #F4F4F5;
		color: #909399;
	}

	.status-success {
		background-color: #E8F6F1;
		color: #67C23A;
	}

	.status-failed {
		background-color: #FEF0F0;
		color: #F56C6C;
	}

	/* 操作类型标签 */
	.operation-badge {
		padding: 4rpx 16rpx;
		border-radius: 20rpx;
		font-size: 24px;
		text-align: center;
	}

	.operation-add {
		background-color: #E8F6F1;
		color: #67C23A;
	}

	.operation-edit {
		background-color: #E6F3FF;
		color: #4A90E2;
	}

	.operation-delete {
		background-color: #FEF0F0;
		color: #F56C6C;
	}

	.operation-query {
		background-color: #F0F2FF;
		color: #6366F1;
	}

	.operation-export {
		background-color: #FDF6EC;
		color: #E6A23C;
	}

	.operation-import {
		background-color: #F4F4F5;
		color: #909399;
	}

	/* 操作按钮 */
	.action-buttons {
		display: flex;
	}

	.edit-btn,
	.status-btn,
	.detail-btn {
		display: flex;
		align-items: center;
		padding: 6rpx 16rpx;
		border-radius: 6rpx;
		margin-right: 20rpx;
		transition: background-color 0.3s;
	}

	.edit-btn {
		background-color: #E6F3FF;
		color: #4A90E2;
	}

	.edit-btn:hover {
		background-color: #D1E8FF;
	}

	.enable-btn {
		background-color: #E8F6F1;
		color: #67C23A;
	}

	.enable-btn:hover {
		background-color: #D8F0E6;
	}

	.disable-btn {
		background-color: #FEF0F0;
		color: #F56C6C;
	}

	.disable-btn:hover {
		background-color: #FDDCDC;
	}

	.delete-btn {
		background-color: #FEF0F0;
		color: #F56C6C;
	}

	.delete-btn:hover {
		background-color: #FDDCDC;
	}

	.detail-btn {
		background-color: #F0F2FF;
		color: #6366F1;
	}

	.detail-btn:hover {
		background-color: #E6E8FF;
	}

	.action-buttons text {
		margin-left: 8rpx;
		font-size: 24px;
	}

	/* 分页 */
	.pagination {
		display: flex;
		justify-content: center;
		align-items: center;
		margin-top: 40rpx;
	}

	.page-btn {
		display: flex;
		align-items: center;
		padding: 10rpx 20rpx;
		border: 2rpx solid #DCDFE6;
		border-radius: 8rpx;
		margin: 0 10rpx;
		font-size: 28px;
		color: #606266;
		background-color: #FFFFFF;
		transition: all 0.3s;
	}

	.page-btn:hover:not(.disabled) {
		border-color: #4A90E2;
		color: #4A90E2;
	}

	.page-btn.disabled {
		color: #C0C4CC;
		cursor: not-allowed;
	}

	.prev-btn {
		margin-right: 20rpx;
	}

	.next-btn {
		margin-left: 20rpx;
	}

	.page-number {
		width: 70rpx;
		height: 70rpx;
		line-height: 70rpx;
		text-align: center;
		border-radius: 8rpx;
		margin: 0 10rpx;
		font-size: 28px;
		color: #606266;
		background-color: #FFFFFF;
		border: 2rpx solid #DCDFE6;
		transition: all 0.3s;
	}

	.page-number:hover:not(.active) {
		border-color: #4A90E2;
		color: #4A90E2;
	}

	.page-number.active {
		background-color: #4A90E2;
		color: #FFFFFF;
		border-color: #4A90E2;
	}
</style>