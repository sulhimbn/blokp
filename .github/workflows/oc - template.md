name: oc - main

on:
  schedule:
    - cron: '* * * * *'
  workflow_dispatch:

# Widen only what the agent in this job actually does, and list every grant in
# ALLOWED in scripts/check_workflow_permissions.py or `workflow-permissions`
# fails the build. Do not add `id-token: write`: nothing here mints an OIDC
# token, and it was copied out of this template into ten workflows (#560).
permissions:
  contents: read
  pull-requests: read
  issues: read

# global lock: only 1 instance of this workflow running across events
concurrency:
  group: ${{ github.workflow }}-global
  cancel-in-progress: false

jobs:
  opencode:
    name: OC
    runs-on: ubuntu-slim
    timeout-minutes: 40
    permissions:
      contents: read
      pull-requests: read
      issues: read
      
    env:
      GH_TOKEN: ${{ secrets.GH_TOKEN }}
      IFLOW_API_KEY: ${{ secrets.IFLOW_API_KEY }}
      
    steps:
      - name: Checkout
        uses: actions/checkout@v5
        with:
          fetch-depth: 1
      - name: Install OpenCode CLI
        run: |
          curl -fsSL https://opencode.ai/install | bash
          echo "$HOME/.opencode/bin" >> $GITHUB_PATH
      - name: Run OpenCode1
        id: run_agent1
        timeout-minutes: 20
        run: |
          opencode run "$(cat <<'PROMPT'
            ========================================
            PERAN
            ========================================
            Anda adalah

            ========================================
            KEMAMPUAN
            ========================================
            1. 
            ----------------------------------------
            - 
            - 
            - 

            2. 
            ----------------------------------------
            - 
            - 
            - 
            - 

            3. 
            ----------------------------------------
            - 
            - 
            - 
            - 
            - 
            4. 
            ----------------------------------------
            - 
            - 
            - 

            5. 
            ----------------------------------------
            - 
            - 
            - 
            - 
            ========================================
            TANGGUNG JAWAB
            ========================================
            1. 
            ----------------------------------------
            - 
            - 
            - 

            2. 
            ----------------------------------------
            - 
            - 
            - 
            - 

            3. 
            ----------------------------------------
            - 
            - 
            - 
            - 
            - 
            4. 
            ----------------------------------------
            - 
            - 
            - 

            5. 
            ----------------------------------------
            - 
            - 
            - 
            - 
            ========================================
            LANGKAH KERJA
            ========================================
            1. 
            ----------------------------------------
            - 
            - 
            - 

            2. 
            ----------------------------------------
            - 
            - 
            - 
            - 

            3. 
            ----------------------------------------
            - 
            - 
            - 
            - 
            - 
            4. 
            ----------------------------------------
            - 
            - 
            - 

            5. 
            ----------------------------------------
            - 
            - 
            - 
            - 
            ========================================
            BATASAN
            ========================================
            1. 
            ----------------------------------------
            - 
            - 
            - 

            2. 
            ----------------------------------------
            - 
            - 
            - 
            - 

            3. 
            ----------------------------------------
            - 
            - 
            - 
            - 
            - 
            4. 
            ----------------------------------------
            - 
            - 
            - 

            5. 
            ----------------------------------------
            - 
            - 
            - 
            - 

          PROMPT
          )" \
            --model iflowcn/glm-4.6 \
            --share false \
