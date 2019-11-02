#!/usr/bin/env python3

"""
Check the domains in top-1m.csv to find out which use WordPress.

By default, 20 domains are checked simultaneously.
This number can be changed with `-j`.
"""

import argparse
import asyncio
import collections
import csv
import os
import re
import socket
import sys
import traceback
from typing import Any, List

import aiohttp

FILENAME = "resources/top-1m.csv"

HEADERS = {"User-Agent": "Firefox"}

HTML_REGEX = rb"meta.*generator.*WordPress"


async def wordpress_used(args: Any, rows: List[List[str]]) -> None:
    parallel_requests: int = args.parallel_requests
    timeout_sec: int = args.timeout

    next_row = 0

    async def write_out():
        try:
            while True:
                await asyncio.sleep(1)
                status = collections.Counter(row[2] for row in rows)
                total = len(rows)
                done = total - status["nil"]
                true = status["true"] / done
                # false = status["false"] / done
                timeout = status["timeout"] / done
                print(
                    f"\r\x1b[K{done}/{total} {true:.4%} WP, {timeout:.4%} timeout",
                    end="",
                    flush=True,
                )
                with open(FILENAME + "~", "w") as fp:
                    csv.writer(fp).writerows(rows)
                os.rename(FILENAME + "~", FILENAME)
                if next_row == len(rows):
                    return
        except Exception:
            print("Exception occurred inside write_out")
            traceback.print_exc()
            sys.exit(1)

    async def requester():
        nonlocal next_row

        while next_row < len(rows):
            row = rows[next_row]
            next_row += 1
            if row[2] != "nil":
                continue
            domain = row[1]
            try:
                async with client.get(f"http://{domain}") as response:
                    resp = await response.read()
                    if re.search(HTML_REGEX, resp):
                        row[2] = "true"
                    else:
                        row[2] = "false"
            except asyncio.TimeoutError:
                row[2] = "timeout"
            except aiohttp.client_exceptions.TooManyRedirects:
                row[2] = "TooManyRedirects"
            except aiohttp.client_exceptions.ClientConnectorCertificateError:
                row[2] = "ClientConnectorCertificateError"
            except aiohttp.client_exceptions.ClientConnectorError as e:
                os_error = getattr(e, "os_error", None)
                if isinstance(os_error, socket.gaierror):
                    row[2] = "socket.gaierror"
                elif isinstance(os_error, ConnectionRefusedError):
                    row[2] = "ConnectionRefusedError"
                else:
                    print(
                        "\rClientConnectorError %r %r while trying domain %s"
                        % (e, os_error, domain)
                    )
            except Exception as e:
                print(f"\rException {e!r} while trying domain {domain}")

    try:
        timeout = aiohttp.ClientTimeout(total=timeout_sec)
        async with aiohttp.ClientSession(headers=HEADERS, timeout=timeout) as client:
            coros = [requester() for _ in range(parallel_requests)]
            await asyncio.gather(write_out(), *coros)
    finally:
        next_row = len(rows)


parser = argparse.ArgumentParser()
parser.add_argument("-j", "--parallel-requests", type=int, default=20)
parser.add_argument("-t", "--timeout", type=int, default=10)


def main() -> None:
    args = parser.parse_args()

    with open(FILENAME) as fp:
        rows = list(csv.reader(fp))
    for row in rows:
        if len(row) == 2:
            row.append("nil")
    asyncio.run(wordpress_used(args, rows))


if __name__ == "__main__":
    main()
