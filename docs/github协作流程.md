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

## 2. 个人分支分配规则

建议由组长统一创建或约定每位成员的个人分支，例如：

| 序号 | 姓名   | 分支         |
| ---- | :----- | ------------ |
| 1    | 孔文峰 | member/kwf   |
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

开始新任务前，先同步本地 `main`：即始终在自己的分支上进行开发，而不是在main分支上开发

```bash
git checkout main
git pull origin main
```

然后切换到自己的个人分支，并把最新 `main` 同步进去：

```bash
git checkout member/zhangsan
git merge main
```

## 5. 任务与分支关系

本仓库采用如下关系：

- 每位协作者长期维护一个个人分支
- 每个 Issue 对应一个任务
- 每个任务在协作者自己的个人分支上完成
- 每个任务完成后，从个人分支向 `main` 发起一个 PR

示例：

- 张三负责 Issue `#4`，在 `member/zhangsan` 上开发
- 李四负责 Issue `#5`，在 `member/lisi` 上开发

也就是说，协作者不再执行：

```bash
git checkout -b docs/4-update-readme
```

而是始终在自己的个人分支上工作：

```bash
git checkout member/zhangsan
```

## 6. 修改文件

开发时直接在自己的个人分支上修改文件即可，例如：

- `README.md`
- `docs/team.md`
- `CONTRIBUTING.md`
- 代码目录中的源文件

## 7. 添加文件到暂存区

推荐只添加本次任务相关文件，避免误提交无关内容。

```bash
git add README.md
git add README.md docs/team.md
```

如果你非常确定当前目录下所有改动都需要提交，也可以使用：

```bash
git add .
```

## 9. 提交 Commit

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

## 10. 推送个人分支到 GitHub

如果你已经在本地和远程建立了个人分支的跟踪关系，提交后直接推送即可：

```bash
git push
```

如果是第一次在本地推送你的个人分支，可以使用：

```bash
git push -u origin member/zhangsan
```

其中 `-u` 用于建立本地分支和远程分支的关联关系。

## 11. 创建 Pull Request

推送完成后，进入 GitHub 仓库页面创建 Pull Request。

需要确认合并方向正确：

```text
base: main <- compare: member/zhangsan
```

也就是说：

- `base` 是 `main`
- `compare` 是你自己的个人分支

## 12. PR 标题与描述

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

## 13. Assignee 和 Reviewer 规则

- Assignee 设置为提交者本人
- Reviewer 至少指定 1 名其他组员
- Reviewer 不能是自己
- Review 通过后再合并

例如：

- Assignee：李四
- Reviewer：张三

## 14. PR 创建后继续修改

如果 PR 创建后还需要补充修改，不要重新开分支，也不要重新开 PR，继续在自己的个人分支上提交即可：

```bash
git checkout member/zhangsan
git add README.md
git commit -m "docs: address review comments"
git push
```

推送后，原 PR 会自动更新。

## 15. Review 与合并

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

## 16. 合并后的处理

PR 合并后，不再删除个人分支。  
因为个人分支是长期使用的固定分支，后续任务还要继续复用。

合并完成后，建议回到本地执行：

```bash
git checkout main
git pull origin main
git checkout member/zhangsan
git merge main
```

这样可以保证你的个人分支继续与最新 `main` 保持同步。

## 17. 常见问题

### 17.1 忘记切到个人分支，直接在 `main` 修改了怎么办

如果还没提交，可以先切到自己的个人分支，当前修改会一起带过去：

```bash
git checkout member/zhangsan
git add README.md
git commit -m "docs: update README"
git push
```

### 17.2 提交前想撤销未提交修改

撤销单个文件：

```bash
git restore README.md
```

撤销所有未提交修改：

```bash
git restore .
```

请谨慎使用，撤销后修改将丢失。

### 17.3 文件已经 `git add` 了，想取消暂存

取消单个文件暂存：

```bash
git restore --staged README.md
```

取消全部暂存：

```bash
git restore --staged .
```

### 17.4 Commit message 写错了，但还没 push

可以修改最近一次提交信息：

```bash
git commit --amend -m "docs: update README"
```

如果已经 push，初学者不建议修改历史，建议再补一个修正 commit。

### 17.5 `main` 更新了，我的个人分支落后了怎么办

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

## 18. 冲突解决基本流程

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

## 19. 每个任务的标准命令模板

以后每次做任务时，可以直接套用以下流程：

```bash
git checkout main
git pull origin main
git checkout member/你的分支名
git merge main

# 修改文件

git status
git diff
git add 修改的文件
git commit -m "类型: 简要说明"
git push
```

然后到 GitHub 页面：

```text
Compare & pull request
-> 确认 base: main
-> 确认 compare: 你的个人分支
-> 填写 PR 标题和描述
-> 指定 Assignee 和 Reviewer
-> Create pull request
-> Review
-> Squash and merge
```

## 20. 最短版流程

如果只想记住最核心命令，保留下面这一套即可：

```bash
git checkout main
git pull origin main
git checkout member/zhangsan
git merge main

# 修改文件

git add README.md
git commit -m "docs: update README"
git push
```

然后在 GitHub 页面：

```text
Create Pull Request
-> compare 选择你的个人分支
-> 指定 Reviewer
-> Review
-> Squash and merge
```
