# 课程小组 GitHub 协作开发规范

本文档适用于课程小组使用 GitHub 协作开发的场景。  
本仓库采用的协作方式不是“每个任务新建一个分支”，而是：

- 组长创建仓库并邀请组员成为 Collaborator
- 每位协作者分配一个固定的长期个人分支
- 每个任务先创建一个 Issue
- 每个任务在协作者自己的固定分支上完成
- 开发完成后从个人分支提交 Pull Request 到 `main`
- 至少 1 名组员 Review 通过后再合并
- 合并后保留个人分支，后续任务继续复用该分支
- 项目任务通过 GitHub Project 看板跟踪进度

## 1. 协作规则

1. 所有开发都必须在预先分配好的个人分支上进行，禁止直接在 `main` 分支上开发。
2. 每位协作者只使用自己的固定分支提交代码，不自行创建新的任务分支。
3. 每个任务都应先创建一个 Issue，再开始开发。
4. 一个 Issue 对应一次任务开发和一次 Pull Request，但不再对应一个新分支。
5. 分支推送到 GitHub 后，由提交者本人创建 Pull Request。
6. PR 创建者需要将自己设为 Assignee，并指定至少 1 名 Reviewer。
7. Reviewer 不能是 PR 创建者本人。
8. PR 通过 Review 后，默认使用 `Squash and merge` 合并到 `main`。
9. PR 合并后不删除个人分支，后续任务继续在该个人分支上开发。
10. 所有任务都应加入 GitHub Project 看板，并根据进度在 `Todo`、`In progress`、`Review`、`Done` 四个状态之间流转。

## 2. 个人分支分配规则

建议由组长统一创建或约定每位成员的个人分支，例如：

| 序号 | 姓名   | 分支         |
| ---- | :----- | ------------ |
| 1    | 孔文峰 | `member/kwf` |
| 2    | 陈思远 | `member/csy` |
| 3    | 毛小斌 | `member/mxb` |
| 4    | 王子嘉 | `member/wzj` |
| 5    | 王子儒 | `member/wzr` |
| 6    | 叶高平 | `member/ygp` |
| 7    | 支佳璇 | `member/zjx` |
| 8    | 支梦林 | `member/zml` |

## 3. 第一次加入项目

首次参与项目时，先克隆仓库：

```bash
git clone https://github.com/组长用户名/oss-course-2026.git
cd oss-course-2026
git status
git branch -a
```

然后查看远程是否已经有分配给你的个人分支，例如：

```text
remotes/origin/main
remotes/origin/member/zhangsan
remotes/origin/member/lisi
```

如果你的个人分支是 `member/zhangsan`，第一次在本地切换并建立跟踪关系：

```bash
git checkout -b member/zhangsan origin/member/zhangsan
```

之后可以用下面命令确认当前分支：

```bash
git branch
```

## 4. 每次开始新任务前

开始新任务前，先同步本地 `main`：

```bash
git checkout main
git pull origin main
```

然后切换到自己的个人分支，并把最新 `main` 同步进去：

```bash
git checkout member/zhangsan
git merge main
```

这样可以保证你始终在自己的个人分支上开发，而不是在 `main` 上开发。

## 5. 创建和认领任务

每个新任务都应先在 GitHub 上创建一个 Issue，写清楚：

- 任务目标
- 背景说明
- 具体工作内容
- 验收标准

创建 Issue 后，应立即将该 Issue 加入 GitHub Project 看板，并将状态设为：

```text
Todo
```

这表示：

- 任务已经提出
- 任务已进入项目管理流程
- 但还没有正式开始开发

如果任务已经明确由某位成员负责，可以在 Issue 中设置 Assignee，并在看板中保留在 `Todo`，直到真正开始动手开发。

## 6. 任务与分支关系

本仓库采用如下关系：

- 每位协作者长期维护一个个人分支
- 每个 Issue 对应一个任务
- 每个任务在协作者自己的个人分支上完成
- 每个任务完成后，从个人分支向 `main` 发起一个 PR

示例：

- 张三负责 Issue `#4`，在 `member/zhangsan` 上开发
- 李四负责 Issue `#5`，在 `member/lisi` 上开发

始终在自己的个人分支上工作：

```bash
git checkout member/zhangsan
```

## 7. 正式开始开发时如何使用看板

当你已经认领任务，并且准备正式开始修改代码或文档时，应先把 Project 看板中的任务状态从：

```text
Todo
```

改为：

```text
In progress
```

这一步表示：

- 该任务已经有人在处理
- 任务已进入实际开发阶段
- 其他成员不应重复认领同一任务

建议顺序如下：

1. 先同步 `main`
2. 切换到自己的个人分支
3. 将看板状态改为 `In progress`
4. 再开始修改文件

对应命令：

```bash
git checkout main
git pull origin main
git checkout member/zhangsan
git merge main
```

## 8. 修改文件

开发时直接在自己的个人分支上修改文件即可，例如：

- `README.md`
- `docs/team.md`
- `CONTRIBUTING.md`
- 代码目录中的源文件

## 9. 添加文件到暂存区

**（建议直接使用这个）**

```bash
git add .
```

## 10. 提交 Commit

推荐提交信息格式：

```text
类型: 简要说明
```

示例：

```bash
git commit -m "docs: update README"
git commit -m "docs: add team information"
git commit -m "feat: add user login api"
git commit -m "fix: handle empty username"
git commit -m "test: add user login tests"
```

不建议使用以下提交信息：

```text
update
修改
111
final
```

## 11. 推送个人分支到 GitHub

使用：

```bash
git push -u origin member/zhangsan
```

## 12. 创建 Pull Request

推送完成后，进入 GitHub 仓库页面创建 Pull Request。

需要确认合并方向正确：

```text
base: main <- compare: member/zhangsan
```

也就是说：

- `base` 是 `main`
- `compare` 是你自己的个人分支

确认无误后，点击：

```
Create pull request 
```

创建 PR 后，进入当前 PR 页面，在当前页面继续完善以下信息。

## 13. PR 标题与描述

PR 标题建议与本次任务内容和 commit 说明对应，例如：

```text
docs: update README
feat: add login api
fix: handle empty username
```

PR 描述建议使用以下模板：

```md
## 修改了什么
简要说明本次改动内容。

## 关联 Issue
Closes #4

## 如何测试
- [x] 已检查主要功能、页面或文档
- [x] 已确认修改内容正常

## 是否更新了文档
- [x] 是
- [ ] 否
```

如果没有关联 Issue，可以写 `无`，但课程项目建议尽量每个任务都对应一个 Issue。

## 14. 在 PR 页面右侧边栏设置 Assignee、Reviewer、Project 和 Label

创建 Pull Request 后，在页面右侧边栏依次设置以下内容：

### 14.1 Assignees

Assignees 表示该 PR 的负责人。

设置要求：

- Assignee 设置为提交者本人

操作方式：

1. 在右侧找到 Assignees
2. 点击该区域
3. 搜索并选择自己

例如：

- Assignee：李四

### 14.2 Reviewers

Reviewers 表示负责审核该 PR 的组员。

设置要求：

- 至少指定 1 名其他组员
- Reviewer 不能是自己

操作方式：

1. 在右侧找到 Reviewers
2. 点击该区域
3. 搜索并选择其他组员

例如：

- Reviewer：张三

### 14.3 Labels

Labels 用于标记该 PR 的类型，方便分类和筛选。

建议按任务类型选择，例如：

- documentation：文档修改
- bug：问题修复
- enhancement：功能增强
- feature：新功能开发

操作方式：

1. 在右侧找到 Labels
2. 点击该区域
3. 选择与本次任务相符的标签

示例：

- 修改 README：选择 documentation
- 修复登录错误：选择 bug

### 14.4 Projects

Projects 用于把该 PR 加入 GitHub Project 看板，便于统一跟踪任务进度。

操作方式：

1. 在右侧找到 Projects
2. 点击该区域
3. 选择当前课程项目使用的 Project 看板
4. 将该任务状态设置为：

```
Review 
```

这表示：

- 该任务已经完成开发
- 已经提交 PR
- 当前正在等待组员审核


## 15. PR 创建后继续修改

如果 PR 创建后还需要补充修改，不要重新开分支，也不要重新开 PR，继续在自己的个人分支上提交即可：

```bash
git checkout member/zhangsan
git add README.md
git commit -m "docs: address review comments"
git push
```

推送后，原 PR 会自动更新。

如果任务仍在等待审核或根据审核意见修改，那么看板状态保持为：

```text
Review
```

不需要改回 `In progress`。

## 16. Review 与合并

Reviewer 检查完成后：

1. 打开 `Files changed`
2. 点击 `Review changes`
3. 选择 `Approve`
4. 提交 Review

合并时建议选择：

```text
Squash and merge
```

合并提交信息建议保持简洁，例如：

```text
docs: update README
```

## 17. 合并后的处理

PR 合并后，不再删除个人分支。  
因为个人分支是长期使用的固定分支，后续任务还要继续复用。

合并完成后，回到本地执行：

```bash
git checkout member/zhangsan
git fetch --all
git checkout main
git pull origin main
git merge member/zhangsan
git push -u origin main
```

这样可以保证你的个人分支继续与最新 `main` 保持同步。

## 18. Project 看板四个状态的使用总结

本项目看板状态统一按以下方式使用：

- `Todo`：Issue 已创建，但还没正式开始开发
- `In progress`：已经开始在个人分支上处理任务
- `Review`：已经提交 PR，等待审核或根据审核意见继续修改
- `Done`：PR 已合并到 `main`，任务正式完成

推荐流转顺序：

```text
Todo -> In progress -> Review -> Done
```

## 19. 常见问题

### 19.1 忘记切到个人分支，直接在 `main` 修改了怎么办

如果还没提交，可以先切到自己的个人分支，当前修改会一起带过去：

```bash
git checkout member/zhangsan
git add README.md
git commit -m "docs: update README"
git push
```

### 19.2 提交前想撤销未提交修改

撤销单个文件：

```bash
git restore README.md
```

撤销所有未提交修改：

```bash
git restore .
```

请谨慎使用，撤销后修改将丢失。

### 19.3 文件已经 `git add` 了，想取消暂存

取消单个文件暂存：

```bash
git restore --staged README.md
```

取消全部暂存：

```bash
git restore --staged .
```

### 19.4 Commit message 写错了，但还没 push

可以修改最近一次提交信息：

```bash
git commit --amend -m "docs: update README"
```

如果已经 push，初学者不建议修改历史，建议再补一个修正 commit。

### 19.5 `main` 更新了，我的个人分支落后了怎么办

先同步 `main`，再把 `main` 合并到你的个人分支：

```bash
git checkout main
git pull origin main
git checkout member/zhangsan
git merge main
```

如果有冲突，解决后执行：

```bash
git add .
git commit -m "chore: resolve merge conflicts"
git push
```

## 20. 冲突解决基本流程

发生冲突时，Git 会在文件中标出类似内容：

```text
<<<<<<< HEAD
你的分支内容
=======
main 分支的内容
>>>>>>> main
```

你需要手动编辑成最终保留的版本，并删除以下标记：

- `<<<<<<<`
- `=======`
- `>>>>>>>`

解决后提交：

```bash
git add 冲突文件名
git commit -m "chore: resolve merge conflicts"
git push
```

## 21. 每个任务的标准命令模板

以后每次做任务时，可以直接套用以下流程：

```bash
git checkout main
git pull origin main
git checkout member/你的分支名
git merge main

# 在 Project 看板中把任务从 Todo 改为 In progress
# 修改文件

git status
git diff
git add 修改的文件
git commit -m "类型: 简要说明"
git push
```

然后到 GitHub 页面：

```text
Create Pull Request
-> 确认 base: main
-> 确认 compare: 你的个人分支
-> 在 Project 看板中把任务改为 Review
-> 填写 PR 标题和描述
-> 指定 Assignee 和 Reviewer
-> Create pull request
-> Review
-> Squash and merge
-> 在 Project 看板中把任务改为 Done
```

## 22. 最短版流程

如果只想记住最核心命令，保留下面这一套即可：

```bash
git checkout main
git pull origin main
git checkout member/zhangsan
git merge main

# 在看板中将任务改为 In progress
# 修改文件

git add README.md
git commit -m "docs: update README"
git push
```

然后在 GitHub 页面：

```text
Create Pull Request
-> compare 选择你的个人分支
-> 在看板中改为 Review
-> 指定 Reviewer
-> Review
-> Squash and merge
-> 在看板中改为 Done
```
