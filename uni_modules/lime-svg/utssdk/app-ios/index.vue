<template>
	<view class="defaultStyles">
	</view>
</template>
<script lang="uts">
	/**
   * 引用 iOS 系统库
   * [可选实现，按需引入]
   */
	import { UIImage, UIView, UIImageView } from "UIKit"
	import { setSvgImage } from './utils'
	
	//原生提供以下属性或方法的实现
	export default {
		data() {
			return {};
		},
		/**
		 * 组件名称，也就是开发者使用的标签
		 */
		name: "l-svg-x",
		/**
		 * 组件涉及的事件声明，只有声明过的事件，才能被正常发送
		 */
		emits: ['click'],
		/**
		 * 属性声明，组件的使用者会传递这些属性值到组件
		 */
		props: {
			"src": {
				type: String,
				default: ""
			},
			"color": {
				type: String,
				default: ""
			}
		},
		/**
		 * 组件内部变量声明
		 */

		/**
		 * 属性变化监听器实现
		 */
		watch: {
			"src": {
				handler(newValue : String, oldValue : String) {
					this.update(true)
				},
				immediate: false
			},
			"color": {
				/**
				 * 这里监听属性变化，并进行组件内部更新
				 */
				handler(newValue : String, oldValue : String) {
					if (this.color == '') return
					this.update(false)
				},
				immediate: false
			},
		},
		/**
		 * 规则：如果没有配置expose，则methods中的方法均对外暴露，如果配置了expose，则以expose的配置为准向外暴露
		 * ['publicMethod'] 含义为：只有 `publicMethod` 在实例上可用
		 */
		// expose: [],
		methods: {
			update(flag : boolean) {
				setSvgImage(this.src, this.color, (image: UIImage|null)=>{
					if(image != null){
						this.$el.image = image
						if(flag){
							this.$emit('load')
						}
						return
					}
					this.$emit('error')
				})
			}
		},
		/**
		 * 组件被创建，组件第一个生命周期，
		 * 在内存中被占用的时候被调用，开发者可以在这里执行一些需要提前执行的初始化逻辑
		 * [可选实现]
		 */
		created() {

		},
		/**
		 * 对应平台的view载体即将被创建，对应前端beforeMount
		 * [可选实现]
		 */
		NVBeforeLoad() {

		},
		/**
		 * 创建原生View，必须定义返回值类型
		 * 开发者需要重点实现这个函数，声明原生组件被创建出来的过程，以及最终生成的原生组件类型
		 * [必须实现]
		 */
		NVLoad() : UIImageView {
			let imageView = new UIImageView()
			imageView.contentMode = UIView.ContentMode.scaleAspectFit
			imageView.isUserInteractionEnabled  = true
			return imageView
		},

		/**
		 * 原生View已创建
		 * [可选实现]
		 */
		NVLoaded() {
			/**
			 * 通过 this.$el 来获取原生控件。
			 */
			this.update(true)
		},
		/**
		 * 原生View布局完成
		 * [可选实现]
		 */
		NVLayouted() {

		},
		/**
		 * 原生View将释放
		 * [可选实现]
		 */
		NVBeforeUnload() { },
		/**
		 * 原生View已释放，这里可以做释放View之后的操作
		 * [可选实现]
		 */
		NVUnloaded() {

		},
		/**
		 * 组件销毁
		 * [可选实现]
		 */
		unmounted() { }

		/**
		 * 更多组件开发的信息详见：https://uniapp.dcloud.net.cn/plugin/uts-component.html
		 */
	}
</script>

<style>

</style>